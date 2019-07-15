package sk.csirt.viruschecker.gateway.routing.service

import sk.csirt.viruschecker.gateway.persistence.service.ScanReportService
import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse

class CachedDriverScanService(
    private val scanReportService: ScanReportService,
    private val scanService: DefaultDriverScanService
) : DriverScanService {
    override suspend fun scanFile(scanParams: ScanParameters): FileMultiScanResponse {
        val result = scanService.scanFile(scanParams)
        scanReportService.save(result)
        return result
    }
}