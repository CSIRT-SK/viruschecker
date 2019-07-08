package sk.csirt.viruschecker.driver.payload

import sk.csirt.viruschecker.driver.antivirus.FileScanReport

data class FileScanResponse(
    val filename: String,
    val antivirus: String,
    val status: Status
) {
    enum class Status {
        OK, INFECTED, NOT_AVAILABLE
    }
}

fun FileScanReport.toCheckResponse() = FileScanResponse(
    filename = filename,
    antivirus = antivirus.commonName,
    status = FileScanResponse.Status.valueOf(status.name)
)