package sk.csirt.viruschecker.client.reporting

import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import java.io.File
import java.nio.charset.Charset

class DefaultReporter(private val file: File): Reporter {
    override fun saveReport(result: FileMultiScanResponse) {
        FileUtils.writeStringToFile(file, result.toString(), Charset.defaultCharset())
    }
}