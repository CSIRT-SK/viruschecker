package sk.csirt.viruschecker.routing.payload

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
