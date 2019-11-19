package sk.csirt.viruschecker.routing.payload

data class DriverInfoResponse(
    val driverVersion: String,
    val antivirus: String
)