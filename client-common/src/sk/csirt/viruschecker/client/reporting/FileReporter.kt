package sk.csirt.viruschecker.client.reporting

import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import java.io.File
import java.nio.charset.Charset

class FileReporter(private val file: File): Reporter {
    override fun saveReport(scanResponse: FileHashScanResponse) {
        FileUtils.writeStringToFile(file, scanResponse.toString(), Charset.defaultCharset())
    }
}