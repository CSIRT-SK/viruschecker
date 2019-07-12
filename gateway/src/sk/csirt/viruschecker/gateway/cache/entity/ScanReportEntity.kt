package sk.csirt.viruschecker.gateway.cache.entity

import sk.csirt.viruschecker.hash.HashHolder
import java.time.Instant

data class ScanReportEntity(
    val sha256: String,
    val filename: String,
    val date: Instant,
    val otherHashes: List<HashHolder>,
    val reports: List<AntivirusReportEntity>
)

data class AntivirusReportEntity(
    val antivirus: String,
    val status: String,
    val malwareDescription: String
)