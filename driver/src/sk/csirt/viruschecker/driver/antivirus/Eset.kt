package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.config.AntivirusType
import java.io.File
import java.nio.charset.Charset

class Eset(
    scanCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val antivirusName: String = AntivirusType.ESET.antivirusName

    override suspend fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Report {
        val reports = withContext(Dispatchers.IO) {
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
                            "result=\"\"" == status -> ScanStatusResult.NOT_AVAILABLE
                            "OK" in status -> ScanStatusResult.OK
                            else -> ScanStatusResult.INFECTED
                        },
                        status.split("=")[1].let {
                            it.substring(1, it.length - 1)
                        }
                    )
                }
            }

        val status = reports.maxBy { it.status }?.status ?: return Report(
            status = ScanStatusResult.NOT_AVAILABLE,
            malwareDescription = ""
        )

        return reports.filter { it.status == status }
            .reduce { acc, report ->
                acc.copy(
                    status = status,
                    malwareDescription = "${acc.malwareDescription}, ${report.malwareDescription}"
                )
            }
    }
}
