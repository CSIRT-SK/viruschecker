package sk.csirt.viruschecker.client.reporting

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.ZoneId

class CsvReporter(private val file: File) : Reporter {
    override fun saveReport(result: FileHashScanResponse) {
        data class ReportLine(
            val file: String,
            val date: LocalDateTime,
            val antivirus: String,
            val status: String,
            val malwareDescription: String,
            val sha256: String,
            val sha1: String,
            val md5: String
        )

        FileWriter(file).use { fileWriter ->
            CSVPrinter(
                fileWriter, CSVFormat.DEFAULT.withHeader(
                    "file", "date", "antivirus", "status",
                    "malwareDescription", "sha256", "sha1", "md5"
                )
            ).use { csvPrinter ->
                result.report.results.map {
                   listOf(
                        result.report.filename,
                        LocalDateTime.ofInstant(result.report.date, ZoneId.systemDefault()),
                        it.antivirus,
                        it.status.name,
                        it.malwareDescription,
                        result.sha256,
                        result.sha1,
                        result.md5
                    )
                }.forEach { csvPrinter.printRecord(it) }
            }
        }
    }
}