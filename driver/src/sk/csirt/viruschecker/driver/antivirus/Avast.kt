package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sk.csirt.viruschecker.driver.config.AntivirusType
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset

class Avast(
    scanCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val type: AntivirusType = AntivirusType.AVAST

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
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?.let { line ->
                line.split("\t")[1].let {
                    Report(
                        when {
                            "OK" in it -> ScanStatusResult.OK
                            else -> ScanStatusResult.INFECTED
                        },
                        it
                    )
                }
            } ?: Report(ScanStatusResult.NOT_AVAILABLE, "")
}
