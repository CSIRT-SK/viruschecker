package sk.csirt.viruschecker.gateway.service.payload

data class AntivirusDriverResponse(
    val filename: String,
    val antivirus: String,
    val status: Status,
    val malwareDescription: String
) {
    enum class Status {
        OK, INFECTED, NOT_AVAILABLE
    }
}