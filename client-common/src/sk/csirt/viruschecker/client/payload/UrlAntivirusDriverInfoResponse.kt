package sk.csirt.viruschecker.client.payload

import sk.csirt.viruschecker.routing.payload.AntivirusDriverInfoResponse

data class UrlAntivirusDriverInfoResponse(
    val url: String,
    val success: Boolean,
    val info: AntivirusDriverInfoResponse
)