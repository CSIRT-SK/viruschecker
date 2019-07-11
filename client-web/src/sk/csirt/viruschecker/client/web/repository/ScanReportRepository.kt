package sk.csirt.viruschecker.client.web.repository

import sk.csirt.viruschecker.client.web.repository.entity.ScanReportEntity

interface ScanReportRepository {
    fun save(item: ScanReportEntity): ScanReportEntity

    fun find(id: String): ScanReportEntity?
}