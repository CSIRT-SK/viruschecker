package sk.csirt.viruschecker.driver.antivirus

import mu.KotlinLogging
import sk.csirt.viruschecker.driver.config.Constants
import sk.csirt.viruschecker.driver.utils.ProcessRunner
import java.nio.file.Paths
import java.util.*

typealias AntivirusOutput = List<String>

abstract class CommandLineAntivirus(
    private val scanCommand: RunProgramCommand,
    private val processRunner: ProcessRunner
) : Antivirus, AutoDetectable {
    private val logger = KotlinLogging.logger { }

    override suspend fun scanFile(params: FileScanParameters): FileScanResult {
        logger.info("Scanning file with $antivirusName antivirus and parameters: $params")
        val output = runAntivirusToScan(params)
        return retrieveReport(output, params).also {
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

        val result = runAntivirus(scanCommand.parse(params.fileToScan, reportFile))
        if (reportFile.exists()) {
            runCatching {
                reportFile.delete()
            }.onFailure {
                logger.warn { "Cannot delete report file $reportFile" }
            }
        }
        return result
    }

    private suspend fun runAntivirus(command: List<String>): AntivirusOutput {
        logger.debug("Waiting for $antivirusName. Command to run: $scanCommand")
        val report = processRunner.runProcess(command)
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