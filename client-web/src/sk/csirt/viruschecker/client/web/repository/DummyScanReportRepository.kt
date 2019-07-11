package sk.csirt.viruschecker.client.web.repository

import sk.csirt.viruschecker.client.web.repository.entity.ScanReportEntity
import java.util.concurrent.ConcurrentHashMap

object DummyScanReportRepository : ScanReportRepository {

    private val storage = ConcurrentHashMap<String, ScanReportEntity>()

    override fun save(item: ScanReportEntity): ScanReportEntity {
        storage[item.id] = item
        return item
    }

    override fun find(id: String): ScanReportEntity? = storage[id]
}