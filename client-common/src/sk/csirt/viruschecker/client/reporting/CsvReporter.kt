package sk.csirt.viruschecker.client.reporting

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.ZoneId

class CsvReporter(private val file: File) : Reporter {
    override fun saveReport(result: FileMultiScanResponse) {
        data class ReportLine(
            val file: String,
            val date: LocalDateTime,
            val antivirus: String,
            val status: String,
            val malwareDescription: String
        )

        FileWriter(file).use { fileWriter ->
            CSVPrinter(
                fileWriter, CSVFormat.DEFAULT.withHeader(
                    "file", "date", "antivirus", "status", "malwareDescription"
                )
            ).use { csvPrinter ->
                result.reports.map {
                    ReportLine(
                        file = result.filename,
                        date = LocalDateTime.ofInstant(result.date, ZoneId.systemDefault()),
                        antivirus = it.antivirus,
                        malwareDescription = it.malwareDescription,
                        status = it.status.name
                    )
                }.also { csvPrinter.printRecord(it) }
            }
        }
    }
}