package sk.csirt.viruschecker.routing.payload

data class AntivirusDriverInfoResponse(
    val driverVersion: String,
    val antivirus: String
)
