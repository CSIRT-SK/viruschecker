package sk.csirt.viruschecker.client.web.repository.entity

import sk.csirt.viruschecker.hash.Hash
import java.time.Instant

data class ScanReportEntity(
    val id: String,
    val filename: String,
    val date: Instant,
    val fileHashes: List<Hash>,
    val reports: List<AntivirusReportEntity>
)

data class AntivirusReportEntity(
    val antivirus: String,
    val status: String,
    val malwareDescription: String
)