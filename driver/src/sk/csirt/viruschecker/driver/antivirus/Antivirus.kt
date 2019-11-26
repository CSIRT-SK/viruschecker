package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import java.io.File

val logger = KotlinLogging.logger { }

interface Antivirus {
    val antivirusName: String

    suspend fun scanFile(params: FileScanParameters): FileScanResult

    suspend fun scanFileAndClean(params: FileScanParameters)
            : FileScanResult = supervisorScope {
        scanFile(params).also {
            launch(IO) {
                params.fileToScan.delete()
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun CoroutineScope.scanFileChannel(params: FileScanParameters)
            : ReceiveChannel<AntivirusReportResult> = produce {
        scanFile(params).scanReport.reports.forEach { send(it) }
    }

    @ExperimentalCoroutinesApi
    suspend fun CoroutineScope.scanFileChannelAndClean(params: FileScanParameters)
            : ReceiveChannel<AntivirusReportResult> = produce {
        scanFileAndClean(params).scanReport.reports.forEach { send(it) }
    }
}

data class FileScanParameters(
    val fileToScan: File,
    val originalFileName: String = fileToScan.name,
    val externalServicesAllowed: Boolean
)

data class ScanResult(
    val status: ScanStatusResult,
    val reports: List<AntivirusReportResult>
) {
    constructor(
        reports: List<AntivirusReportResult>
    ) : this(
        reports = reports,
        status = reports.maxBy { it.status }?.status ?: ScanStatusResult.NOT_AVAILABLE
    )
}

data class FileScanResult(
    val filename: String,
    val scanReport: ScanResult
)

data class AntivirusReportResult(
    val antivirusName: String,
    val virusDatabaseVersion: String,
    val status: ScanStatusResult,
    val malwareDescription: String
)

/**
 * Do not change the order of constants!
 */
enum class ScanStatusResult {
    SCAN_REFUSED, NOT_AVAILABLE, OK, INFECTED
}