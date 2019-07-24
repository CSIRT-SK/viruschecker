package sk.csirt.viruschecker.gateway.persistence.service

import sk.csirt.viruschecker.routing.payload.FileHashScanResponse

interface ScanReportService {
    fun save(response: FileHashScanResponse)
    fun findBySha256(hash: String): FileHashScanResponse?
}