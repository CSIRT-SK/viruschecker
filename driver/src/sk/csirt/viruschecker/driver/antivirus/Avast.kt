package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime

class Avast(
    scanCommand: ExecutableCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val type: AntivirusType = AntivirusType.AVAST

    override fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Sequence<ReportEntry> {
        // Avast seems to fill report files with data after the the ashCmd.exe returns.
        // The line below is a poor way to handle this.
//        sleep(500)
        return sequenceOf(
            FileUtils.readLines(reportFile, Charset.defaultCharset())
                .takeIf { it.isNotEmpty() }
                ?.first()
                ?.let { line ->
                    line.split("\t")[1].let {
                        ReportEntry(
                            datetime = LocalDateTime.now(),
                            status = when {
                                "OK" in it -> ReportEntry.Status.OK
                                else -> ReportEntry.Status.INFECTED
                            },
                            description = it
                        )
                    }
                } ?: ReportEntry(
                datetime = LocalDateTime.now(),
                status = ReportEntry.Status.NOT_AVAILABLE,
                description = ""
            )
        )
    }
}
