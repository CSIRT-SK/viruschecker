package sk.csirt.viruschecker.client.payload

import sk.csirt.viruschecker.hash.Hash
import java.time.Instant

data class FileMultiScanResponse(
    val date: Instant,
    val filename: String,
    val status: ScannedFileStatus,
    val fileHashes: List<Hash>,
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