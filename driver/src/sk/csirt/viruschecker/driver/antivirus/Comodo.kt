package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.config.AntivirusType
import java.io.File
import java.nio.charset.Charset

class Comodo(scanCommand: RunProgramCommand) : CommandLineAntivirus(scanCommand) {

    override val type: AntivirusType = AntivirusType.COMODO

    override suspend fun parseReportFile(
        reportFile: File,
        params: FileScanParameters
    ): Report = withContext(Dispatchers.IO) {
        FileUtils.readLines(
            reportFile,
            Charset.defaultCharset()
        )
    }[1].split("--->")[1]
        .trim()
        .let {
            when {
                it == "Not Virus" ->
                    Report(ScanStatusReport.OK, it)
                it.startsWith("Found Virus") ->
                    Report(ScanStatusReport.INFECTED, it.split(" is ")[1])
                else -> Report(ScanStatusReport.NOT_AVAILABLE, "")
            }
        }
}
