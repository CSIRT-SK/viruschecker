package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType

class Kaspersky(
    scanCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    override val antivirusName: String = AntivirusType.KASPERSKY.antivirusName

    override suspend fun parseReport(
        rawReport: List<String>,
        params: FileScanParameters
    ): Report {
        val reports = rawReport
            .asSequence()
            .filterNot { it.startsWith(";") }
            .filter { params.fileToScan.name in it }
            .map { line ->
                line.split("\t").let {
                    Report(
                        status = when (it[2].trim()) {
                            "ok" -> ScanStatusResult.OK
                            "detected" -> ScanStatusResult.INFECTED
                            else -> ScanStatusResult.NOT_AVAILABLE
                        },
                        malwareDescription = if (it.size > 3) it[3].trim() else "OK",
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
                virusDatabaseVersion = rawReport.first().substring("AV bases release date: ".length)
            )
    }

}