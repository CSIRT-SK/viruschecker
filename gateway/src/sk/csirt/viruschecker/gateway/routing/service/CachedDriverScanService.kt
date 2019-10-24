package sk.csirt.viruschecker.gateway.routing.service

import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanChannel
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse

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
            val resultChannel = scanService.scanFileChannel(scanParams)
            val saveChannel = produce<AntivirusReportResponse> {
                for (antivirusResponse in resultChannel) {
                    scanReportService.save(antivirusResponse)
                    send(antivirusResponse)
                }
            }
            resultChannel.copy(
                report = resultChannel.report.copy(results = saveChannel)
            )
        }
}