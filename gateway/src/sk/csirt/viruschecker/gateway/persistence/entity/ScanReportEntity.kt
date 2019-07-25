package sk.csirt.viruschecker.gateway.persistence.entity

import java.io.Serializable
import java.time.Instant

data class ScanReportEntity(
    val sha256: String,
    val sha1: String,
    val md5: String,
    val filename: String,
    val date: Instant,
    val reports: List<AntivirusReportEntity>
): Serializable

data class AntivirusReportEntity(
    val antivirus: String,
    val status: String,
    val malwareDescription: String
): Serializable
