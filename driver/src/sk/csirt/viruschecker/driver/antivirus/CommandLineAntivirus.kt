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
import java.util.*

typealias AntivirusOutput = List<String>

data class RunProgramCommand(
    val command: String
) {

    fun parse(
        fileToScan: File? = null,
        fileToReport: File? = null
    ) = command.split(" ").map {
        when {
            SCAN_FILE in it ->
                it.replace(SCAN_FILE, fileToScan?.canonicalPath ?: "")
            REPORT_FILE in it ->
                it.replace(REPORT_FILE, fileToReport?.canonicalPath ?: "")
            else -> it
        }
    }

    companion object Placeholder {
        const val SCAN_FILE = "[SCAN-FILE]"
        const val REPORT_FILE = "[REPORT-FILE]"
    }
}

abstract class CommandLineAntivirus(
    private val scanCommand: RunProgramCommand
) : Antivirus {
    private val logger = KotlinLogging.logger { }

    override suspend fun scanFile(params: FileScanParameters): FileScanReport = coroutineScope {
        logger.info("Scanning file with this parameters: $params")
        val reportFile = Paths.get(
            Constants.scanReportsDir,
            "report-${params.fileToScan.nameWithoutExtension}-${UUID.randomUUID()}.txt"
        ).toFile()
        val output = runAntivirusToScan(params, reportFile)
        // Some antiviruses (Avast) cannot write results properly when invoked from another process.
        // This will manually write their STDOUT to the file
        writeOutputToFileIfNotExists(reportFile, output)
        retrieveReport(reportFile, params).also {
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

    private suspend fun runAntivirusToScan(params: FileScanParameters, reportFile: File)
            : AntivirusOutput = runAntivirus(scanCommand.parse(params.fileToScan, reportFile))

    protected suspend fun runAntivirus(command: List<String>): AntivirusOutput {
        val processBuilder = ProcessBuilder()
        processBuilder.command(command)
        logger.debug("Waiting for antivirusName. Command to run: $scanCommand")
        val output = withContext(Dispatchers.IO) {
            val process = processBuilder.start()
            val processReader = BufferedReader(InputStreamReader(process.inputStream))
            val output = mutableListOf<String>()
            var line: String? = processReader.readLine()
            var i = 1
            while (line != null) {
                logger.debug("Output line [$i] from antivirusName: $line")
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
        reportFile: File,
        params: FileScanParameters
    ): FileScanReport {
        val (status, description) = parseReportFile(reportFile, params)
        return FileScanReport(
            filename = params.originalFileName,
            scanReport = ScanReport(
                antivirusType = type,
                status = status,
                reports = listOf(
                    AntivirusReport(
                        status = status,
                        malwareDescription = description,
                        antivirusName = type.antivirusName
                    )
                )
            )
        )
    }

    protected abstract suspend fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Report

    protected data class Report(val status: ScanStatusReport, val malwareDescription: String)

}
