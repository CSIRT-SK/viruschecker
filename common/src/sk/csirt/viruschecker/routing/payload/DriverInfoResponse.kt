package sk.csirt.viruschecker.routing.payload

data class DriverInfoResponse(
    val driverVersion: String,
    val usesExternalServices: Boolean,
    val antivirus: String
)
