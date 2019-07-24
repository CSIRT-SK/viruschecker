//package sk.csirt.viruschecker.gateway.virustotal
//
//import com.kanishka.virustotalv2.VirusTotalConfig
//import com.kanishka.virustotalv2.VirustotalPublicV2Impl
//import mu.KotlinLogging
//import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
//import sk.csirt.viruschecker.gateway.persistence.service.ScanReportService
//import sk.csirt.viruschecker.routing.payload.ScanResultResponse
//import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
//import sk.csirt.viruschecker.routing.payload.FileScanResponse
//import sk.csirt.viruschecker.routing.payload.ScanStatusResponse
//import java.time.Instant
//import java.time.format.DateTimeFormatter
//
//
//class VirusTotalScanReportService(
//    private val apiKey: String?,
//    private val persistentScanReportService: PersistentScanReportService
//) : ScanReportService by persistentScanReportService {
//
//    private val logger = KotlinLogging.logger { }
//
//    init {
//        VirusTotalConfig.getConfigInstance().virusTotalAPIKey = apiKey
//    }
//
//    override fun findBySha256(hash: String): FileHashScanResponse? {
//        val retrieved = retrieve(hash)
//        val stored = persistentScanReportService.findBySha256(hash)
//
//        return when {
//            stored == null -> retrieved
//            retrieved == null -> stored
//            else -> stored.copy(
//                report = stored.report.copy(
//                    date = maxOf(stored.report.date, retrieved.report.date),
//                    status = maxOf(stored.report.status, retrieved.report.status),
//                    results = stored.report.results + retrieved.report.results
//                )
//            )
//        }
//    }
//
//    private fun retrieve(
//        hash: String
//    ): FileHashScanResponse? {
//        val virusTotalRef = VirustotalPublicV2Impl()
//        val scanInformationResult = runCatching { virusTotalRef.getScanReport(hash) }
//            .onFailure { logger.error { "VirusTotal error for hash $hash. Cause: ${it.stackTrace}" } }
//            .onSuccess { logger.info { "VirusTotal success for hash $hash." } }
//
//        val scanInformationResultIfFound = scanInformationResult.getOrNull()
//
//        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//        return when {
//            scanInformationResultIfFound == null -> null
//            "Scan finished" in scanInformationResultIfFound.verboseMessage ->
//                scanInformationResultIfFound.let {
//                    FileHashScanResponse(
//                        sha256 = it.sha256,
//                        md5 = it.md5,
//                        sha1 = it.sha1,
//                        report = FileScanResponse(
//                            date = dateTimeFormatter.parse(it.scanDate, Instant::from),
//                            filename = "hash",
//                            results = it.scans.map { (antivirus, scanInfo) ->
//                                ScanResultResponse(
//                                    antivirus = "$antivirus (VirusTotal)",
//                                    malwareDescription = scanInfo.result,
//                                    status = if (scanInfo.isDetected)
//                                        ScanStatusResponse.INFECTED
//                                    else
//                                        ScanStatusResponse.OK
//                                )
//                            }
//                        )
//                    )
//                }
//            else -> null
//        }
//
//    }
//}