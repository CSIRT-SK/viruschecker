package sk.csirt.viruschecker.driver.antivirus

import com.kanishka.virustotalv2.VirusTotalConfig
import com.kanishka.virustotalv2.VirustotalPublicV2Impl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.hash.sha256

class VirusTotal(apiKey: String) : Antivirus {

    private val logger = KotlinLogging.logger { }

    init {
        VirusTotalConfig.getConfigInstance().virusTotalAPIKey = apiKey
    }

    override val type: AntivirusType = AntivirusType.VIRUS_TOTAL

    override suspend fun scanFile(params: FileScanParameters): FileScanReport {
        val virusTotalRef = VirustotalPublicV2Impl()
        val fileToScan = params.fileToScan
        val sha256 = withContext(Dispatchers.IO) { fileToScan.sha256() }.value
        val scanInformation =
            runCatching { withContext(Dispatchers.IO) { virusTotalRef.getScanReport(sha256) } }
                .onFailure {
                    logger.error {
                        "VirusTotal error for file ${fileToScan.canonicalPath}. " +
                                "Cause: ${it.stackTrace}"
                    }
                }
                .onSuccess { logger.info { "VirusTotal success for file ${fileToScan.canonicalPath}." } }
                .getOrThrow()

        val emptyReport = FileScanReport(
            filename = params.originalFileName,
            scanReport = ScanReport(
                antivirusType = type,
                status = ScanStatusReport.NOT_AVAILABLE,
                reports = listOf(
                    AntivirusReport(
                        antivirusName = type.antivirusName,
                        malwareDescription = "${type.antivirusName} did not recognize this hash",
                        status = ScanStatusReport.NOT_AVAILABLE
                    )
                )
            )
        )

        return when {
            scanInformation == null -> emptyReport
            "Scan finished" in scanInformation.verboseMessage ->
                scanInformation.let {
                    FileScanReport(
                        filename = params.originalFileName,
                        scanReport = ScanReport(
                            antivirusType = type,
                            reports = it.scans.map { (antivirus, info) ->
                                AntivirusReport(
                                    antivirusName = "$antivirus (${type.antivirusName})",
                                    malwareDescription = info.result ?: "",
                                    status = when {
                                        info.result == null -> ScanStatusReport.NOT_AVAILABLE
                                        info.isDetected -> ScanStatusReport.INFECTED
                                        else -> ScanStatusReport.OK
                                    }
                                )
                            }
                        )
                    )

                }
            else -> emptyReport
        }
    }
}