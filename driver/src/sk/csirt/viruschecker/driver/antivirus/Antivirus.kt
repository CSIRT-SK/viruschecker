package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType
import mu.KotlinLogging
import sk.csirt.viruschecker.hash.HashHolder
import java.io.File

val logger = KotlinLogging.logger { }

interface Antivirus {
    val type: AntivirusType

    suspend fun scanFile(params: FileScanParameters): FileScanReport
}

data class FileScanParameters(
    val fileToScan: File,
    val originalFileName: String = fileToScan.name
)

data class ScanReport(
    val antivirusType: AntivirusType,
    val status: ScanStatusReport,
    val reports: List<AntivirusReport>
){
    constructor(
        antivirusType: AntivirusType,
        reports: List<AntivirusReport>
    ) : this(
        antivirusType = antivirusType,
        reports = reports,
        status = reports.maxBy { it.status }?.status ?: ScanStatusReport.NOT_AVAILABLE
    )
}

data class FileScanReport(
    val filename: String,
    val scanReport: ScanReport
)

data class AntivirusReport(
    val antivirusName: String,
    val status: ScanStatusReport,
    val malwareDescription: String
)

/**
 * Do not change the order of constants!
 */
enum class ScanStatusReport {
    NOT_AVAILABLE, OK, INFECTED
}
