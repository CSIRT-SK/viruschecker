package sk.csirt.viruschecker.client.service

import sk.csirt.viruschecker.client.payload.FileMultiScanResponse
import java.io.File

interface ScanService {
    suspend fun scanFile(fileToScan: File): FileMultiScanResponse
}