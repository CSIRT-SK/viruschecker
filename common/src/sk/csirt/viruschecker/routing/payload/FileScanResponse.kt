package sk.csirt.viruschecker.routing.payload

import java.time.Instant


data class FileHashScanResponse(
    val sha256: String,
    val md5: String,
    val sha1: String,
    val report: FileScanResponse
)

data class FileScanResponse(
    val date: Instant,
    val filename: String,
    val status: ScanStatus,
    val results: List<AntivirusReportResponse>
) {
    constructor(
        date: Instant,
        filename: String,
        results: List<AntivirusReportResponse>
    ) : this(
        date = date,
        filename = filename,
        results = results,
        status = results.maxBy { it.status }?.status ?: ScanStatus.NOT_AVAILABLE
    )
}

data class AntivirusReportResponse(
    val antivirus: String,
    val status: ScanStatus,
    val malwareDescription: String,
    val virusDatabaseVersion: String
)

/**
 * Do not change the order of constants!
 */
enum class ScanStatus {
    SCAN_REFUSED, NOT_AVAILABLE, OK, INFECTED
}