package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse

interface Reporter{
    fun saveReport(result: FileMultiScanResponse)
}