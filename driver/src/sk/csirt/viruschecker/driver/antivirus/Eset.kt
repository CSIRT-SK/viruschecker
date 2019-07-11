package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime

class Eset(
    scanCommand: ExecutableCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val type: AntivirusType = AntivirusType.ESET

    override fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Sequence<ReportEntry> {
        val linesWithScannedFile = FileUtils.readLines(reportFile, Charset.defaultCharset())
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