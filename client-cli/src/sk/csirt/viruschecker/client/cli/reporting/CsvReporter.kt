package reporting

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import sk.csirt.viruschecker.client.cli.payload.FileScanResponse
import sk.csirt.viruschecker.client.cli.reporting.Reporter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.ZoneId

class CsvReporter : Reporter<FileScanResponse> {
    override fun saveReport(file: File, results: List<FileScanResponse>) {
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
                results.flatMap { result ->
                    result.reports.map {
                        ReportLine(
                            file = result.filename,
                            date = LocalDateTime.ofInstant(result.date, ZoneId.systemDefault()),
                            antivirus = it.antivirus,
                            malwareDescription = it.malwareDescription,
                            status = it.status.name
                        )
                    }
                }.forEach {
                    csvPrinter.printRecord(it)
                }
            }
        }
    }
}