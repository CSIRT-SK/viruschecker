package sk.csirt.viruschecker.gateway.persistence.entity

import sk.csirt.viruschecker.routing.payload.ScanStatus
import java.io.Serializable
import java.time.Instant

data class ScanReportEntity(
    val sha256: String,
    val sha1: String,
    val md5: String,
    val filename: String,
    val date: Instant,
    val reports: Iterable<AntivirusReportEntity>
): Serializable

data class AntivirusReportEntity(
    val antivirus: String,
    val status: ScanStatus,
    val malwareDescription: String,
    val virusDatabaseVersion: String
): Serializable