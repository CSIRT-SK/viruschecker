package sk.csirt.viruschecker.gateway.service

import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import java.io.File

interface ScanService {
    suspend fun scanFile(fileToScan: File): FileMultiScanResponse
}