package sk.csirt.viruschecker.gateway.persistence.repository

import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity

interface ScanReportRepository {
    suspend fun save(item: ScanReportEntity): ScanReportEntity

    suspend fun findBySha256(hash: String): ScanReportEntity?

    suspend fun findAll(): Iterable<ScanReportEntity>
}