package sk.csirt.viruschecker.gateway.routing.service

import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
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
}