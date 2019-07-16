package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.config.AntivirusType
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime

class Eset(
    scanCommand: ScanCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val type: AntivirusType = AntivirusType.ESET

    override suspend fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Sequence<ReportEntry> {
        val linesWithScannedFile = withContext(Dispatchers.IO) { FileUtils.readLines(reportFile, Charset.defaultCharset()) }
            .also { logger.debug { "From ${reportFile.name} loaded report: $it" } }
            .asSequence()
            .filter { it.startsWith("name=") }
            .filter { params.fileToScan.name in it }
            .also { logger.debug("Lines ${it.toList()}") }
        return linesWithScannedFile
            .map { line ->
                line.split(", ")[1].let { status ->
                    ReportEntry(
                        datetime = LocalDateTime.now(),
                        status = when {
                            "result=\"\"" == status -> ReportEntry.Status.NOT_AVAILABLE
                            "OK" in status -> ReportEntry.Status.OK
                            else -> ReportEntry.Status.INFECTED
                        },
                        description = status.split("=")[1].let {
                            it.substring(1, it.length - 1)
                        }
                    )
                }
            }
    }
}

