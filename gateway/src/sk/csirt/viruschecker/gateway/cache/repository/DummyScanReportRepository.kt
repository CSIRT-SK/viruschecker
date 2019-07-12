package sk.csirt.viruschecker.gateway.cache.repository

import sk.csirt.viruschecker.gateway.cache.entity.ScanReportEntity
import java.util.concurrent.ConcurrentHashMap

object DummyScanReportRepository : ScanReportRepository {

    private val storage = ConcurrentHashMap<String, ScanReportEntity>()

    override fun save(item: ScanReportEntity): ScanReportEntity {
        storage[item.sha256] = item
        return item
    }

    override fun findBySha256(hash: String): ScanReportEntity? = storage[hash]
}