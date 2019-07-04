package driver.antivirus

import driver.config.AntivirusType
import io.ktor.util.asStream
import kotlinx.io.core.Input
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Path
import java.util.*

val logger = KotlinLogging.logger {  }

interface Antivirus {
    val type: AntivirusType

    fun scanFile(params: FileScanParameters): FileScanReport
}

data class FileScanParameters(
    val fileToScan: File,
    val originalFileName: String = fileToScan.name
)

data class FileScanReport(
    val filename: String,
    val antivirus: AntivirusType,
    val status: Status,
    val malwareDescription: String
) {
    enum class Status {
        OK, INFECTED, NOT_AVAILABLE
    }
}