package gateway.payload

data class AntivirusResponse(
        val filename: String,
        val antivirus: String,
        val status: Status
) {
        enum class Status {
                OK, INFECTED, NOT_AVAILABLE
        }
}