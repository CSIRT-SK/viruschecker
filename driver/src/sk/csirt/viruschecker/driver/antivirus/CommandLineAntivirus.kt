package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.config.Constants
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.*


data class ScanCommand(
    val command: String
) {

    fun parse(fileToScan: File, fileToReport: File) =
        command.split(" ").map {
            when {
                SCAN_FILE in it -> it.replace(SCAN_FILE, fileToScan.canonicalPath)
                REPORT_FILE in it -> it.replace(REPORT_FILE, fileToReport.canonicalPath)
                else -> it
            }
        }

    companion object Placeholder {
        const val SCAN_FILE = "[SCAN-FILE]"
        const val REPORT_FILE = "[REPORT-FILE]"
    }
}

abstract class CommandLineAntivirus(
    private val scanCommand: ScanCommand
) : Antivirus {
    private val logger = KotlinLogging.logger { }

    override suspend fun scanFile(params: FileScanParameters): FileScanReport = coroutineScope {
        logger.info("Scanning file with this parameters: $params")
        val reportFile = Paths.get(
            Constants.scanReportsDir,
            "report-${params.fileToScan.nameWithoutExtension}-${UUID.randomUUID()}.txt"
        ).toFile()
        val output = runAntivirus(params, reportFile)
        // Some antiviruses (Avast) cannot write reports properly when invoked from another process.
        // This will manually write their STDOUT to the file
        writeOutputToFileIfNotExists(reportFile, output)
        retrieveReport(params, reportFile).also {
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

    private suspend fun runAntivirus(params: FileScanParameters, reportFile: File)
            : List<String> {
        val processBuilder = ProcessBuilder()
        processBuilder.command(scanCommand.parse(params.fileToScan, reportFile))

        logger.debug("Waiting for antivirus. Command to run: $scanCommand")
        val output = withContext(Dispatchers.IO) {
            val process = processBuilder.start()
            val processReader = BufferedReader(InputStreamReader(process.inputStream))
            val output = mutableListOf<String>()
            var line: String? = processReader.readLine()
            var i = 1
            while (line != null) {
                logger.debug("Output line [$i] from antivirus: $line")
                output += line
                line = processReader.readLine()
                i++
            }
            output
        }
        logger.debug("Antivirus task completed. Command successfully executed: $scanCommand")
        return output
    }

    private suspend fun retrieveReport(
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

    protected abstract suspend fun parseReportFile(
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
}
