package sk.csirt.viruschecker.routing.payload

import kotlinx.coroutines.channels.ReceiveChannel
import java.time.Instant

data class HashResponse(
    val md5: String,
    val sha1: String,
    val sha256: String
)

data class FileHashScanChannel(
    val sha256: String,
    val md5: String,
    val sha1: String,
    val reportChannel: FileScanChannel
) : ReceiveChannel<AntivirusReportResponse> by reportChannel

data class FileScanChannel(
    val date: Instant,
    val filename: String,
    val resultChannel: ReceiveChannel<AntivirusReportResponse>
) : ReceiveChannel<AntivirusReportResponse> by resultChannel