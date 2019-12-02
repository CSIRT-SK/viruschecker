package sk.csirt.viruschecker.gateway.persistence.repository

import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import java.util.concurrent.ConcurrentMap

class KeyValueScanReportRepository(
    private val storage: ConcurrentMap<String, ScanReportEntity>
) : ScanReportRepository {

    override suspend fun findAll(): Iterable<ScanReportEntity> {
       return storage.values
    }

    override suspend fun save(item: ScanReportEntity): ScanReportEntity {
        storage[item.sha256] = item
        return item
    }

    override suspend fun findBySha256(hash: String): ScanReportEntity? = storage[hash]
}