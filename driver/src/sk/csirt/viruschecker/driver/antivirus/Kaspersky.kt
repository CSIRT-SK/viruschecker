package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sk.csirt.viruschecker.driver.config.AntivirusType
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Kaspersky(
    scanCommand: RunProgramCommand
//        updateCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val type: AntivirusType = AntivirusType.KASPERSKY

    override suspend fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Report = withContext(Dispatchers.IO) {
        FileUtils.readLines(
            reportFile,
            Charset.defaultCharset()
        )
    }.also { logger.debug { "From ${reportFile.name} loaded report: $it" } }
        .asSequence()
        .filterNot { it.startsWith(";") }
        .filter { params.fileToScan.name in it }
        .map { line ->
            line.split("\t").let {
                Report(
                    when (it[2].trim()) {
                        "ok" -> ScanStatusReport.OK
                        "detected" -> ScanStatusReport.INFECTED
                        else -> ScanStatusReport.NOT_AVAILABLE
                    },
                    if (it.size > 3) it[3].trim() else "OK"
                )
            }
        }.maxBy { it.status } ?: Report(ScanStatusReport.NOT_AVAILABLE, "")
}