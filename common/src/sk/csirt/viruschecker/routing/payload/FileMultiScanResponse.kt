package sk.csirt.viruschecker.routing.payload

import sk.csirt.viruschecker.hash.HashHolder
import java.time.Instant

data class FileMultiScanResponse(
    val date: Instant,
    val sha256: String,
    val filename: String,
    val status: ScannedFileStatus,
    val otherHashes: List<HashHolder>,
    val reports: List<AntivirusScanResponse>
)

data class AntivirusScanResponse(
    val antivirus: String,
    val status: ScannedFileStatus,
    val malwareDescription: String
)

/**
 * Do not change the order of constants!
 */
enum class ScannedFileStatus {
    NOT_AVAILABLE, OK, INFECTED
}