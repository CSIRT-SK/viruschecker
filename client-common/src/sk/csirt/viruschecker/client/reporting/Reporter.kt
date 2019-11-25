package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.routing.payload.FileHashScanResponse

interface Reporter{
    fun saveReport(scanResponse: FileHashScanResponse)
}