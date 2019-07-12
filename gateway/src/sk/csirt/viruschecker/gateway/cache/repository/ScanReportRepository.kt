package sk.csirt.viruschecker.gateway.cache.repository

import sk.csirt.viruschecker.gateway.cache.entity.ScanReportEntity

interface ScanReportRepository {
    fun save(item: ScanReportEntity): ScanReportEntity

    fun findBySha256(hash: String): ScanReportEntity?
}