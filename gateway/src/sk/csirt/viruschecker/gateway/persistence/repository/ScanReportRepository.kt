package sk.csirt.viruschecker.gateway.persistence.repository

import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import java.time.Instant

interface ScanReportRepository {
    suspend fun save(item: ScanReportEntity): ScanReportEntity

    suspend fun findBySha256(hash: String): ScanReportEntity?

    suspend fun findAll(): Iterable<ScanReportEntity>

    suspend fun findBetween(
        begin: Instant,
        end: Instant
    ): Iterable<ScanReportEntity> =
        findAll().filter { it.date > begin && it.date < end }
}