package driver.antivirus

import driver.config.AntivirusType
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
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
        val linesWithScannedFile = Files.readAllLines(reportFile.toPath().toAbsolutePath())
            .asSequence()
            .filter { params.fileToScan.name in it }
            .filter { it.startsWith("name=\"") }
        val parsedEntries = linesWithScannedFile
            .map { line ->
                line.split(", ")[1].let {
                    ReportEntry(
                        datetime = LocalDateTime.now(),
                        status = when {
                            "result=\"\"" == it -> ReportEntry.Status.NOT_AVAILABLE
                            "OK" in it -> ReportEntry.Status.OK
                            else -> ReportEntry.Status.INFECTED
                        },
                        description = it.split("=")[1].let {
                            it.substring(0, it.length - 1)
                        }
                    )
                }
            }
        return parsedEntries
    }
}