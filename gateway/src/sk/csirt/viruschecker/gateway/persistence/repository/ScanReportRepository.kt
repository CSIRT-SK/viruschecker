package sk.csirt.viruschecker.gateway.persistence.repository

import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity

interface ScanReportRepository {
    fun save(item: ScanReportEntity): ScanReportEntity

    fun findBySha256(hash: String): ScanReportEntity?
}