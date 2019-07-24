package sk.csirt.viruschecker.gateway.routing.service

import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.MultiScanRequest

interface FileScanService {
    suspend fun scanFile(scanParams: MultiScanRequest): FileHashScanResponse
}

