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
    val status: ScanStatusResponse,
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
        status = results.maxBy { it.status }?.status ?: ScanStatusResponse.NOT_AVAILABLE
    )
}

data class AntivirusReportResponse(
    val antivirus: String,
    val status: ScanStatusResponse,
    val malwareDescription: String
)

/**
 * Do not change the order of constants!
 */
enum class ScanStatusResponse {
    NOT_AVAILABLE, OK, INFECTED
}
