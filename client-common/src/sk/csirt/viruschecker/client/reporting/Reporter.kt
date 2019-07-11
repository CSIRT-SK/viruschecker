package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.client.payload.FileMultiScanResponse

interface Reporter{
    fun saveReport(result: FileMultiScanResponse)
}