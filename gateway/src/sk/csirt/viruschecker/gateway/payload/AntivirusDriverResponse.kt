package sk.csirt.viruschecker.gateway.payload

data class AntivirusDriverResponse(
        val filename: String,
        val antivirus: String,
        val status: Status
) {
        enum class Status {
                OK, INFECTED, NOT_AVAILABLE
        }
}