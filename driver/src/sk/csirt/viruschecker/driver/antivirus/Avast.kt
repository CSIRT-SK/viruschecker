package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.utils.ProcessRunner

class Avast(
    scanCommand: RunProgramCommand,
    processRunner: ProcessRunner
) : CommandLineAntivirus(
    scanCommand,
    processRunner
) {

    override val antivirusName: String = AntivirusType.AVAST.antivirusName

    override suspend fun parseReport(
        rawReport: List<String>,
        params: FileScanParameters
    ): Report =
        rawReport
            .takeIf { it.isNotEmpty() }
            ?.let { rawReport.first() to rawReport.first { "# Virus database" in it } }
            ?.let { (scanLine, databaseLine) ->
                scanLine.split("\t")[1].let {
                    Report(
                        status = when {
                            "OK" in it -> ScanStatusResult.OK
                            else -> ScanStatusResult.INFECTED
                        },
                        malwareDescription = it,
                        virusDatabaseVersion =
                        databaseLine.split(":")[1]
                    )
                }
            } ?: Report(ScanStatusResult.NOT_AVAILABLE, "", "")
}