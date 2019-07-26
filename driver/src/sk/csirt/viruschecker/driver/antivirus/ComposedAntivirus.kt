package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import sk.csirt.viruschecker.driver.exception.AntivirusCompositionException

class Inva

class ComposedAntivirus(private val antiviruses: Iterable<Antivirus>) : Antivirus {

    override val isInternal: Boolean = run {
        val (internal, external) = antiviruses.partition { it.isInternal }

        if (internal.isEmpty() && external.isNotEmpty()) {
            false
        } else if (internal.isNotEmpty() && external.isEmpty()) {
            true
        } else {
            throw AntivirusCompositionException(
                "These antiviruses ${antiviruses.map { it.antivirusName }} are not mutually " +
                        "compatible. Cannot mix ${internal.map { it.antivirusName }} with " +
                        "${external.map { it.antivirusName }}"
            )
        }
    }


    override val antivirusName: String = antiviruses.joinToString(", ") { it.antivirusName }

    override suspend fun scanFile(params: FileScanParameters): FileScanResult {
        val reports = supervisorScope {
            antiviruses.map {
                async(Dispatchers.IO) {
                    it.scanFile(params)
                }
            }.awaitAll()
        }.flatMap { it.scanReport.reports }
        return FileScanResult(
            filename = params.originalFileName,
            scanReport = ScanResult(
                reports = reports
            )
        )
    }

}