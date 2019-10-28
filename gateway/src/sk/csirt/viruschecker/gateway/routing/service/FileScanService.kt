package sk.csirt.viruschecker.gateway.routing.service

import kotlinx.coroutines.CoroutineScope
import sk.csirt.viruschecker.routing.payload.FileHashScanChannel
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import java.io.File

data class ScanParameters(
    val fileToScan: File,
    val originalFilename: String,
    val useExternalDrivers: Boolean
)

interface FileScanService {
    suspend fun scanFile(scanParams: ScanParameters): FileHashScanResponse

    suspend fun CoroutineScope.scanFileChannel(scanParams: ScanParameters): FileHashScanChannel
}

