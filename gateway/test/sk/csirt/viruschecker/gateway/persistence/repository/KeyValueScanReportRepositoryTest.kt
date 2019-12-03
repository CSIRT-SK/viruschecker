package sk.csirt.viruschecker.gateway.persistence.repository

import sk.csirt.viruschecker.gateway.persistence.testScanReportEntities
import java.util.concurrent.ConcurrentHashMap

internal class KeyValueScanReportRepositoryTest : ReportRepositoryTest() {
    override fun prepareRepository(): ScanReportRepository {
        val mockStorage = ConcurrentHashMap(testScanReportEntities.associateBy { it.sha256 })
        return KeyValueScanReportRepository(mockStorage)
    }
}