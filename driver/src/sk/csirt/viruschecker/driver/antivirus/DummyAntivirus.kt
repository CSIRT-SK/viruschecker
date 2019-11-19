package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType
import java.io.FileNotFoundException

class DummyAntivirus : Antivirus {

    override val antivirusName: String = AntivirusType.DUMMY.antivirusName

    private val virusDatabaseVersion = "1.7.0.1"

    override suspend fun scanFile(params: FileScanParameters): FileScanResult = when {
        "eicar" in params.originalFileName -> FileScanResult(
            filename = params.originalFileName,
            scanReport = ScanResult(
                reports = listOf(
                    AntivirusReportResult(
                        antivirusName = antivirusName,
                        status = ScanStatusResult.INFECTED,
                        virusDatabaseVersion = virusDatabaseVersion,
                        malwareDescription = "EICAR-TEST-FILE"
                    )
                )
            )
        )
        "" == params.originalFileName -> throw FileNotFoundException()
        else -> FileScanResult(
            filename = params.originalFileName,
            scanReport = ScanResult(
                reports = listOf(
                    AntivirusReportResult(
                        antivirusName = antivirusName,
                        status = ScanStatusResult.OK,
                        virusDatabaseVersion = virusDatabaseVersion,
                        malwareDescription = "OK"
                    )
                )
            )
        )
    }

}
