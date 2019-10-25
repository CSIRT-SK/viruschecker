package sk.csirt.viruschecker.gateway.routing.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanChannel
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.FileScanResponse

@ExperimentalCoroutinesApi
class CachedDriverScanService(
    private val scanReportService: PersistentScanReportService,
    private val scanService: DefaultDriverScanService
) : FileScanService {
    override suspend fun scanFile(scanParams: ScanParameters): FileHashScanResponse {
        val result = scanService.scanFile(scanParams)
        scanReportService.save(result)
        return result
    }

    override suspend fun scanFileChannel(scanParams: ScanParameters): FileHashScanChannel =
        coroutineScope {
            val scanFileChannel = scanService.scanFileChannel(scanParams)
            val saveReportChannel = produce<AntivirusReportResponse> {
                for (antivirusResponse in scanFileChannel) {
                    val currentScanResponse = scanReportService.findBySha256(scanFileChannel.sha256)
                    scanReportService.save(
                        FileHashScanResponse(
                            md5 = scanFileChannel.md5,
                            sha1 = scanFileChannel.sha1,
                            sha256 = scanFileChannel.sha256,
                            report = FileScanResponse(
                                date = scanFileChannel.reportChannel.date,
                                filename = scanFileChannel.reportChannel.filename,
                                results = (currentScanResponse?.report?.results ?: emptyList())
                                        + antivirusResponse
                            )
                        )
                    )
                    send(antivirusResponse)
                }
            }

            scanFileChannel.copy(
                reportChannel = scanFileChannel.reportChannel.copy(results = saveReportChannel)
            )
        }
}