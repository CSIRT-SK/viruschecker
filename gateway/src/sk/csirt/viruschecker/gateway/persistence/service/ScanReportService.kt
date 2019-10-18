package sk.csirt.viruschecker.gateway.persistence.service

import sk.csirt.viruschecker.routing.payload.FileHashScanResponse

interface ScanReportService {
    suspend fun save(response: FileHashScanResponse)
    suspend fun findBySha256(hash: String): FileHashScanResponse?
    suspend fun findAll(): List<FileHashScanResponse>
    suspend fun findBy(searchWords: Iterable<String>): List<FileHashScanResponse>
}

