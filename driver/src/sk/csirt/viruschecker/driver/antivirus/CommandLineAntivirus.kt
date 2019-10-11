package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import sk.csirt.viruschecker.driver.config.Constants
import java.nio.file.Paths
import java.util.*

typealias AntivirusOutput = List<String>

abstract class CommandLineAntivirus(
    private val scanCommand: RunProgramCommand
) : Antivirus, AutoDetectable {
    private val logger = KotlinLogging.logger { }

    override suspend fun scanFile(params: FileScanParameters): FileScanResult = coroutineScope {
        logger.info("Scanning file with this parameters: $params")
        val output = runAntivirusToScan(params)
        // Some antiviruses (Avast) cannot write results properly when invoked from another process.
        retrieveReport(output, params).also {
            logger.info("Retrieved report: $it")
        }
    }

    override suspend fun isInstalled(): Boolean {
        val antivirusTestRunResult = runCatching {
            runAntivirus(listOf(scanCommand.parse().first()))
        }.getOrElse { return false }
            .joinToString(" ")
        return listOf(
            "is not recognized",
            "No such file or directory"
        ).any { it in antivirusTestRunResult }
            .not()
    }

    private suspend fun runAntivirusToScan(params: FileScanParameters)
            : AntivirusOutput {
        // Some antiviruses (Avast) cannot write results properly to stdout when invoked from
        // another process. Therefore we will use the auxiliary report file. This will cause the AV
        // write its output to both the file and stdout.
        val reportFile = Paths.get(
            Constants.scanReportsDir,
            "report-${params.fileToScan.nameWithoutExtension}-${UUID.randomUUID()}.txt"
        ).toFile()
        return runAntivirus(scanCommand.parse(params.fileToScan, reportFile)).also {
            if (reportFile.exists()) {
                coroutineScope {
                    launch(IO) { runCatching { reportFile.delete() } }
                }
            }
        }
    }

    private suspend fun runAntivirus(command: List<String>): AntivirusOutput {
        logger.debug("Waiting for $antivirusName. Command to run: $scanCommand")
        val report = withContext(IO) {
            ProcessBuilder(command)
                .start()
                .inputStream
                .bufferedReader()
                .useLines {
                    it.toList()
                }
        }
        logger.debug("Antivirus task completed. Command successfully executed: $scanCommand")
        logger.debug("Output from $antivirusName: $report")
        return report
    }

    private suspend fun retrieveReport(
        rawReport: List<String>,
        params: FileScanParameters
    ): FileScanResult {
        val (status, description, virusDatabase) = parseReport(rawReport, params)
        return FileScanResult(
            filename = params.originalFileName,
            scanReport = ScanResult(
                status = status,
                reports = listOf(
                    AntivirusReportResult(
                        status = status,
                        malwareDescription = description,
                        antivirusName = antivirusName,
                        virusDatabaseVersion = virusDatabase
                    )
                )
            )
        )
    }

    protected abstract suspend fun parseReport(
        rawReport: List<String>,
        params: FileScanParameters
    ): Report

    protected data class Report(
        val status: ScanStatusResult,
        val malwareDescription: String,
        val virusDatabaseVersion: String
    )

}
