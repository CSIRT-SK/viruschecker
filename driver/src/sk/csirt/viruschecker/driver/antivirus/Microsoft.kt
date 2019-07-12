package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime

class Microsoft(
    scanCommand: ScanCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val type: AntivirusType = AntivirusType.MICROSOFT

    override fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Sequence<ReportEntry> {
        val lines = FileUtils.readLines(reportFile, Charset.defaultCharset())
            .also { logger.debug { "From ${reportFile.name} loaded report: $it" } }
        val infectedCountString = lines.firstOrNull {
            it.startsWith("Scanning ") &&
                    "found" in it &&
                    it.endsWith(" threats.")
        }?.split(" ")?.let { it[it.size - 2] }
        logger.debug { "Found $infectedCountString threats." }
        val infectedCount = infectedCountString?.toIntOrNull()

        val (status, description) = when {
            infectedCountString == null -> ReportEntry.Status.NOT_AVAILABLE to "NA"
            infectedCountString == "no" -> ReportEntry.Status.OK to "OK"
            infectedCount != null -> {
                val description = lines.first { it.startsWith("Threat") }.split(": ")[1]
                ReportEntry.Status.INFECTED to description
            }
            else -> ReportEntry.Status.NOT_AVAILABLE to "NA"
        }

        return sequenceOf(ReportEntry(
            datetime = LocalDateTime.now(),
            status = status,
            description = description
        ))
    }
}