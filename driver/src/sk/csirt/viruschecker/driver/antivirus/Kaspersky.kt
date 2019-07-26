package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.config.AntivirusType
import java.io.File
import java.nio.charset.Charset

class Kaspersky(
    scanCommand: RunProgramCommand
//        updateCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val antivirusName: String = AntivirusType.KASPERSKY.antivirusName

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
                        "ok" -> ScanStatusResult.OK
                        "detected" -> ScanStatusResult.INFECTED
                        else -> ScanStatusResult.NOT_AVAILABLE
                    },
                    if (it.size > 3) it[3].trim() else "OK"
                )
            }
        }.maxBy { it.status } ?: Report(ScanStatusResult.NOT_AVAILABLE, "")
}