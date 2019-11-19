package sk.csirt.viruschecker.routing.payload

data class ScanFileWebSocketParameters(
    val useExternalServices: Boolean,
    val originalFilename: String
)