package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.utils.ProcessRunner

class Kaspersky(
    scanCommand: RunProgramCommand,
    processRunner: ProcessRunner
) : CommandLineAntivirus(
    scanCommand,
    processRunner
) {

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
                line.split("\t", " ")
                    .filter { it.isNotBlank() }.let {
                        Report(
                            status = when (it[3].trim()) {
                                "ok" -> ScanStatusResult.OK
                                "detected" -> ScanStatusResult.INFECTED
                                else -> ScanStatusResult.NOT_AVAILABLE
                            },
                            malwareDescription = if (it.size > 4) it[4].trim() else "OK",
                            virusDatabaseVersion = ""
                        )
                    }
            }
        val status = reports.maxBy { it.status }?.status ?: return Report(
            status = ScanStatusResult.NOT_AVAILABLE,
            malwareDescription = "",
            virusDatabaseVersion = ""
        )

        val databaseVersion = rawReport.first().substring("AV bases release date: ".length)

        if (status == ScanStatusResult.OK) {
            return Report(
                status = status,
                malwareDescription = "OK",
                virusDatabaseVersion = databaseVersion
            )
        }

        return reports.filter { it.status == status }
            .reduce { acc, reportLine ->
                acc.copy(
                    malwareDescription = "${acc.malwareDescription}, ${reportLine.malwareDescription}"
                )
            }.copy(
                virusDatabaseVersion = databaseVersion
            )
    }

}