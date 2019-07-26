package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

class ComposedAntivirus(private val antiviruses: Iterable<Antivirus>) : Antivirus {
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