package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

class ComposedAntivirus(private val antiviruses: Iterable<Antivirus>) : Antivirus {

//    override val isInternal: Boolean = run {
//        val (internal, external) = antiviruses.partition { it.isInternal }
//
//        if (internal.isEmpty() && external.isNotEmpty()) {
//            false
//        } else if (internal.isNotEmpty() && external.isEmpty()) {
//            true
//        } else {
//            throw AntivirusCompositionException(
//                "These antiviruses ${antiviruses.map { it.antivirusName }} are not mutually " +
//                        "compatible. Cannot mix ${internal.map { it.antivirusName }} with " +
//                        "${external.map { it.antivirusName }}"
//            )
//        }
//    }

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
                                        malwareDescription = "Scan failed."
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