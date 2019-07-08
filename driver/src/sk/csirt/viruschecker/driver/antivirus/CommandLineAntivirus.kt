package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.Constants
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.utils.parseParameter
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.*


data class ExecutableCommand(
    val executableName: String,
    val flag: String,
    val reportFlag: String,
    val additionalOptions: String,
    val timeout: Long
)

abstract class CommandLineAntivirus(
    protected val scanCommand: ExecutableCommand
) : Antivirus {
    private val logger = KotlinLogging.logger { }

    override fun scanFile(params: FileScanParameters): FileScanReport {
        logger.info("Scanning file with this parameters: $params")
        val reportFile = Paths.get(
            Constants.scanReportsDir,
            "report-${params.fileToScan.nameWithoutExtension}-${UUID.randomUUID()}.txt"
        ).toFile()
        val output = runScanCommand(params, reportFile)
//        reportFile.createNewFile()
        // Some antiviruses (Avast) cannot write reports properly when invoked from another process.
        // This will manually write their STDOUT to the file
        writeOutputToFileIfNotExists(reportFile, output)
        return retrieveReport(params, reportFile).also {
            logger.info("Retrieved report: $it")
        }
    }

    private fun writeOutputToFileIfNotExists(
        reportFile: File,
        scanOutput: List<String>
    ) {
        if (reportFile.exists()) return

        reportFile.createNewFile()
        FileUtils.writeLines(reportFile, scanOutput)
    }

    private fun runScanCommand(params: FileScanParameters, reportFile: File): List<String> {
        val command = buildScanCommand(params, scanCommand, reportFile)
        return runAntivirus(command)
    }

    protected fun runAntivirus(command: List<String>): List<String> {
        val processBuilder = ProcessBuilder()
        processBuilder.command(command)
        val process = processBuilder.start()
        logger.debug("Waiting for antivirus. Command to run: $command")
        val processReader = BufferedReader(InputStreamReader(process.inputStream))
        val output = mutableListOf<String>()
        var line: String? = processReader.readLine()
        var i = 1
        while (line != null) {
            logger.debug("Output line [$i] from antivirus: $line")
            output+= line
            line = processReader.readLine()
            i++
        }
        logger.debug("Antivirus task completed. Command successfully executed: $command")
        return output
    }

    private fun retrieveReport(
            params: FileScanParameters,
            reportFile: File
    ): FileScanReport {
        logger.debug("Retrieving report from $reportFile for file $params")
        val parsedEntries = parseReportFile(reportFile, params)
        val nonSkippedEntries = parsedEntries
            .filterNot { it.status == ReportEntry.Status.NOT_AVAILABLE }
        // This is for archive files (zip, rar, ...).
        // Many files in archives may be OK, but it
        // only takes one file to be infected to declare the whole archive as infected.
        val determinedStatus =
            nonSkippedEntries.firstOrNull { it.status == ReportEntry.Status.INFECTED }
                ?: nonSkippedEntries.firstOrNull { it.status == ReportEntry.Status.OK }
        return FileScanReport(
            filename = params.originalFileName,
            status = FileScanReport.Status.valueOf(
                determinedStatus?.status?.name ?: FileScanReport.Status.NOT_AVAILABLE.name
            ),
            antivirus = type,
            malwareDescription = determinedStatus?.description ?: ""
        ).also { logger.debug(it.toString()) }
    }

    protected abstract fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Sequence<ReportEntry>


    protected class ReportEntry(
            val datetime: LocalDateTime,
            val status: Status,
            val description: String
    ) {
        enum class Status {
            OK, INFECTED, NOT_AVAILABLE;

            companion object {
                fun fromCommonName(name: String) = when (name) {
                    "ok" -> OK
                    "detected" -> INFECTED
                    else -> NOT_AVAILABLE
                }
            }
        }
    }

    companion object {
        fun buildScanCommand(
                params: FileScanParameters,
                command: ExecutableCommand,
                reportFile: File
        ): List<String> {
            val commandList = mutableListOf(command.executableName)
            parseParameter(
                commandList,
                command.flag,
                params.fileToScan.canonicalPath
            )
            parseParameter(
                commandList,
                command.reportFlag,
                reportFile.canonicalPath
            )
            command.additionalOptions.takeIf { it.isNotEmpty() }?.also {
                commandList.addAll(it.split(", "))
            }

            return commandList
        }

    }
}
