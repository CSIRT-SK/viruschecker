package gateway.entity

import java.time.Instant

data class FileScanReport(
    val date: Instant,
    val filename: String,
    val reports: List<AntivirusReport>
)

data class AntivirusReport(
    val antivirus: String,
    val status: Status
) {
    enum class Status {
        OK, INFECTED, NOT_AVAILABLE
    }
}
