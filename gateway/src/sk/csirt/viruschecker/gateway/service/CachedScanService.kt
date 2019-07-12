package sk.csirt.viruschecker.gateway.service

import sk.csirt.viruschecker.gateway.cache.service.ScanReportService
import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import java.io.File

class CachedScanService(
  val scanReportService: ScanReportService,
  val scanService: DefaultDriverScanService
) : ScanService {
    override suspend fun scanFile(fileToScan: File): FileMultiScanResponse {
        val result = scanService.scanFile(fileToScan)
        scanReportService.save(result)
        return result
    }
}