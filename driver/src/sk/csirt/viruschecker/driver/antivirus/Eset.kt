package sk.csirt.viruschecker.driver.antivirus

import mu.KotlinLogging
import sk.csirt.viruschecker.driver.config.AntivirusType

class Eset(
    scanCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val antivirusName: String = AntivirusType.ESET.antivirusName

    override suspend fun parseReport(
        report: List<String>,
        params: FileScanParameters
    ): Report {
        val reports = report
            .asSequence()
            .filter { it.startsWith("name=") }
            .filter { params.fileToScan.name in it }
            .also { logger.debug("Lines ${it.toList()}") }
            .map { line ->
                line.split(", ")[1].let { status ->
                    Report(
                        when {
                            "result=\"\"" == status -> ScanStatusResult.NOT_AVAILABLE
                            "OK" in status -> ScanStatusResult.OK
                            else -> ScanStatusResult.INFECTED
                        },
                        status.split("=")[1].let {
                            it.substring(1, it.length - 1)
                        },
                        virusDatabaseVersion = ""
                    )
                }
            }

        val status = reports.maxBy { it.status }?.status ?: return Report(
            status = ScanStatusResult.NOT_AVAILABLE,
            malwareDescription = "",
            virusDatabaseVersion = ""
        )

        return reports.filter { it.status == status }
            .reduce { acc, reportLine ->
                acc.copy(
                    malwareDescription = "${acc.malwareDescription}, ${reportLine.malwareDescription}"
                )
            }.copy(
                virusDatabaseVersion = report.first {
                    "Module scanner" in it
                }.split(",")[1].substring(" version ".length)
            )
    }
}
