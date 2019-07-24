package sk.csirt.viruschecker.client.reporting

import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import java.io.File
import java.nio.charset.Charset

class DefaultReporter(private val file: File): Reporter {
    override fun saveReport(result: FileHashScanResponse) {
        FileUtils.writeStringToFile(file, result.toString(), Charset.defaultCharset())
    }
}