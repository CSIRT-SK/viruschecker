package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType
import mu.KotlinLogging
import java.io.File

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