package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

class ComposedAntivirus(private val antiviruses: Iterable<Antivirus>) : Antivirus {

    override val antivirusName: String = antiviruses.joinToString(", ") { it.antivirusName }

    override suspend fun scanFile(params: FileScanParameters): FileScanResult {
        val reports = supervisorScope {
            antiviruses.map { antivirus ->
                async(IO) {
                    antivirus.scanFile(params)
                } to antivirus.antivirusName
            }.map { (deferredScanResult, antivirusName) ->
                runCatching { deferredScanResult.await() }
                    .getOrDummy(params, antivirusName)
            }
        }.flatMap { it.scanReport.reports }
        return FileScanResult(
            filename = params.originalFileName,
            scanReport = ScanResult(
                reports = reports
            )
        )
    }

    @ExperimentalCoroutinesApi
    override suspend fun CoroutineScope.scanFileChannel(params: FileScanParameters)
            : ReceiveChannel<AntivirusReportResult> = produce {
        antiviruses.map { antivirus ->
            async(IO) {
                val scanResult = runCatching { antivirus.scanFile(params) }
                    .getOrDummy(params, antivirusName)
                scanResult.scanReport.reports.forEach {
                    send(it)
                }
            }
        }.awaitAll()
    }

    private fun Result<FileScanResult>.getOrDummy(
        params: FileScanParameters,
        antivirusName: String
    ): FileScanResult =
        getOrElse { throwable ->
            logger.error(throwable) {
                "Scan failed. Returning status ${ScanStatusResult.NOT_AVAILABLE} for antivirus $antivirusName."
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