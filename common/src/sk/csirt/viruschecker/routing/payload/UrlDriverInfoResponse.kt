package sk.csirt.viruschecker.routing.payload

data class UrlDriverInfoResponse(
    val url: String,
    val success: Boolean,
    val info: DriverInfoResponse
)