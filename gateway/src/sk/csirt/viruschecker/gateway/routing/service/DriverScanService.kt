package sk.csirt.viruschecker.gateway.routing.service

import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import java.io.File

interface DriverScanService {
    suspend fun scanFile(scanParams: ScanParameters): FileMultiScanResponse
}

data class ScanParameters(
    val fileToScan: File,
    val originalFilename: String
)
