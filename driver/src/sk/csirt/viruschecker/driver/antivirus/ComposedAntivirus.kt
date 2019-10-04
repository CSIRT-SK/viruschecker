package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

class ComposedAntivirus(private val antiviruses: Iterable<Antivirus>) : Antivirus {

    override val antivirusName: String = antiviruses.joinToString(", ") { it.antivirusName }

    override suspend fun scanFile(params: FileScanParameters): FileScanResult {
        val reports = supervisorScope {
            antiviruses.map {
                async(Dispatchers.IO) {
                    it.scanFile(params)
                } to it.antivirusName
            }.map { (deferredScanResult, antivirusName) ->
                runCatching { deferredScanResult.await() }
                    .getOrElse {
                        logger.error(it){
                            "Scan failed. Returning status NOT AVAILABLE for antivirus $antivirusName."
                        }
                        FileScanResult(
                            filename = params.originalFileName,
                            scanReport = ScanResult(
                                status = ScanStatusResult.NOT_AVAILABLE,
                                reports = listOf(
                                    AntivirusReportResult(
                                        antivirusName = antivirusName,
                                        status = ScanStatusResult.NOT_AVAILABLE,
                                        malwareDescription = "Scan failed.",
                                        virusDatabaseVersion = ""
                                    )
                                )
                            )
                        )
                    }
            }
        }.flatMap { it.scanReport.reports }
        return FileScanResult(
            filename = params.originalFileName,
            scanReport = ScanResult(
                reports = reports
            )
        )
    }

}