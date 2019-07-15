package sk.csirt.viruschecker.gateway.persistence.repository

import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import java.util.concurrent.ConcurrentMap

class KeyValueScanReportRepository(
    private val storage: ConcurrentMap<CharSequence, ScanReportEntity>
) : ScanReportRepository {

    override fun save(item: ScanReportEntity): ScanReportEntity {
        storage[item.sha256] = item
        return item
    }

    override fun findBySha256(hash: String): ScanReportEntity? = storage[hash]
}