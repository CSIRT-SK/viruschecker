package sk.csirt.viruschecker.driver.antivirus

import com.kanishka.virustotalv2.VirusTotalConfig
import com.kanishka.virustotalv2.VirustotalPublicV2Impl
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.config.DriverPropertiesFactory
import sk.csirt.viruschecker.hash.sha256

class VirusTotal(
    apiKey: String,
    private val virusTotalApiImplementation: VirustotalPublicV2Impl = VirustotalPublicV2Impl()
) : ExternalAntivirus, AutoDetectable {
    private val logger = KotlinLogging.logger { }

    init {
        VirusTotalConfig.getConfigInstance().virusTotalAPIKey = apiKey
    }

    override val antivirusName: String = AntivirusType.VIRUS_TOTAL.antivirusName

    override suspend fun externalScanFile(params: FileScanParameters): FileScanResult {
        val fileToScan = params.fileToScan
        val sha256 = fileToScan.sha256()
        val scanInformation =
            runCatching { withContext(IO) { virusTotalApiImplementation.getScanReport(sha256) } }
                .onFailure {
                    logger.error {
                        "VirusTotal error for file ${fileToScan.canonicalPath}. " +
                                "Cause: ${it.stackTrace}"
                    }
                }.onSuccess {
                    logger.info {
                        "VirusTotal success for file ${fileToScan.canonicalPath}."
                    }
                }.getOrThrow()

        val emptyReport = FileScanResult(
            filename = params.originalFileName,
            scanReport = ScanResult(
                status = ScanStatusResult.NOT_AVAILABLE,
                reports = listOf(
                    AntivirusReportResult(
                        antivirusName = antivirusName,
                        malwareDescription = "$antivirusName did not recognize this hash",
                        status = ScanStatusResult.NOT_AVAILABLE,
                        virusDatabaseVersion = ""
                    )
                )
            )
        )

        return when {
            scanInformation == null -> emptyReport
            "Scan finished" in scanInformation.verboseMessage ->
                FileScanResult(
                    filename = params.originalFileName,
                    scanReport = ScanResult(
                        reports = scanInformation.scans.map { (antivirus, info) ->
                            AntivirusReportResult(
                                antivirusName = "$antivirus ($antivirusName)",
                                malwareDescription = info.result ?: "",
                                status = when {
                                    info.result == null -> ScanStatusResult.NOT_AVAILABLE
                                    info.isDetected -> ScanStatusResult.INFECTED
                                    else -> ScanStatusResult.OK
                                },
                                virusDatabaseVersion = info.version
                            )
                        }
                    )
                )
            else -> emptyReport
        }
    }

    override suspend fun isInstalled(): Boolean =
        VirusTotalConfig.getConfigInstance().virusTotalAPIKey != DriverPropertiesFactory.missingApiKeyPlaceHolder
                && VirusTotalConfig.getConfigInstance().virusTotalAPIKey.isNotBlank()
}