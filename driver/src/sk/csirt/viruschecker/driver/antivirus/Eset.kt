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
    scanCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val type: AntivirusType = AntivirusType.ESET

    override suspend fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Report =
        withContext(Dispatchers.IO) {
            FileUtils.readLines(
                reportFile,
                Charset.defaultCharset()
            )
        }.also { logger.debug { "From ${reportFile.name} loaded report: $it" } }
            .asSequence()
            .filter { it.startsWith("name=") }
            .filter { params.fileToScan.name in it }
            .also { logger.debug("Lines ${it.toList()}") }
            .map { line ->
                line.split(", ")[1].let { status ->
                    Report(
                        when {
                            "result=\"\"" == status -> ScanStatusReport.NOT_AVAILABLE
                            "OK" in status -> ScanStatusReport.OK
                            else -> ScanStatusReport.INFECTED
                        },
                        status.split("=")[1].let {
                            it.substring(1, it.length - 1)
                        }
                    )
                }
            }.maxBy { it.status } ?: Report(ScanStatusReport.NOT_AVAILABLE, "")
}

