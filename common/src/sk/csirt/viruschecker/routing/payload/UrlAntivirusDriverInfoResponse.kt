package sk.csirt.viruschecker.routing.payload

data class UrlAntivirusDriverInfoResponse(
    val url: String,
    val success: Boolean,
    val info: AntivirusDriverInfoResponse
)