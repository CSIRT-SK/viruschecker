package sk.csirt.viruschecker.driver.routing.payload

import sk.csirt.viruschecker.driver.antivirus.FileScanReport

data class FileScanResponse(
    val filename: String,
    val antivirus: String,
    val status: Status,
    val malwareDescription: String
) {
    enum class Status {
        OK, INFECTED, NOT_AVAILABLE
    }
}

fun FileScanReport.toCheckResponse() = FileScanResponse(
    filename = filename,
    antivirus = antivirus.commonName,
    status = FileScanResponse.Status.valueOf(status.name),
    malwareDescription = malwareDescription
)