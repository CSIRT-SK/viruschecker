package sk.csirt.viruschecker.gateway.routing.payload

import java.time.Instant

data class FileScanResponse(
    val date: Instant,
    val filename: String,
    val reports: List<AntivirusResponse>,
    val status: ScannedFileStatus
)

data class AntivirusResponse(
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
