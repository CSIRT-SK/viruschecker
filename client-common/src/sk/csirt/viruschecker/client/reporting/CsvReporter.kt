package sk.csirt.viruschecker.client.reporting

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.ZoneId

class CsvReporter(private val file: File) : Reporter {
    override fun saveReport(scanResponse: FileHashScanResponse) {
        FileWriter(file).use { fileWriter ->
            CSVPrinter(
                fileWriter, CSVFormat.DEFAULT.withHeader(
                    "file", "date", "antivirus", "status",
                    "malwareDescription", "sha256", "sha1", "md5"
                )
            ).use { csvPrinter ->
                scanResponse.report.results.map {
                   listOf(
                        scanResponse.report.filename,
                        LocalDateTime.ofInstant(scanResponse.report.date, ZoneId.systemDefault()),
                        it.antivirus,
                        it.status.name,
                        it.malwareDescription,
                        scanResponse.sha256,
                        scanResponse.sha1,
                        scanResponse.md5
                    )
                }.forEach { csvPrinter.printRecord(it) }
            }
        }
    }
}