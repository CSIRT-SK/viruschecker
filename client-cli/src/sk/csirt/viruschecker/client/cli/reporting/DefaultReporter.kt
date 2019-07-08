package sk.csirt.viruschecker.client.cli.reporting

import org.apache.commons.io.FileUtils
import java.io.File

class DefaultReporter<T> : Reporter<T> {
    override fun saveReport(file: File, results: List<T>) {
        FileUtils.writeLines(file, results)
    }
}