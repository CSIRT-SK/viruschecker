package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sk.csirt.viruschecker.driver.config.AntivirusType
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime

class Microsoft(
    scanCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val type: AntivirusType = AntivirusType.MICROSOFT

    override suspend fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Report {
        val lines = withContext(Dispatchers.IO) {
            FileUtils.readLines(
                reportFile,
                Charset.defaultCharset()
            )
        }
            .also { logger.debug { "From ${reportFile.name} loaded report: $it" } }
        val infectedCountString = lines.firstOrNull {
            it.startsWith("Scanning ") &&
                    "found" in it &&
                    it.endsWith(" threats.")
        }?.split(" ")?.let { it[it.size - 2] }
        logger.debug { "Found $infectedCountString threats." }
        val infectedCount = infectedCountString?.toIntOrNull()

        return when {
            infectedCountString == null -> Report(ScanStatusReport.NOT_AVAILABLE, "NA")
            infectedCountString == "no" -> Report(ScanStatusReport.OK, "OK")
            infectedCount != null -> {
                val description = lines.first { it.startsWith("Threat") }.split(": ")[1]
                Report(ScanStatusReport.INFECTED, description)
            }
            else -> Report(ScanStatusReport.NOT_AVAILABLE, "NA")
        }

    }
}