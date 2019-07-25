package sk.csirt.viruschecker.gateway.persistence.service

import sk.csirt.viruschecker.gateway.persistence.entity.AntivirusReportEntity
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.gateway.persistence.repository.ScanReportRepository
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import sk.csirt.viruschecker.routing.payload.ScanStatusResponse

class PersistentScanReportService(
    private val reportRepository: ScanReportRepository
) : ScanReportService {
    override suspend fun save(response: FileHashScanResponse) {
        ScanReportEntity(
            date = response.report.date,
            filename = response.report.filename,
            sha256 = response.sha256,
            md5 = response.md5,
            sha1 = response.sha1,
            reports = response.report.results.map {
                AntivirusReportEntity(
                    antivirus = it.antivirus,
                    status = it.status.name,
                    malwareDescription = it.malwareDescription
                )
            }
        ).also { reportRepository.save(it) }
    }

    override suspend fun findBySha256(hash: String): FileHashScanResponse? =
        reportRepository.findBySha256(hash)?.let {
            val reports = it.reports.map {
                AntivirusReportResponse(
                    antivirus = it.antivirus,
                    malwareDescription = it.malwareDescription,
                    status = ScanStatusResponse.valueOf(it.status)
                )
            }
            FileHashScanResponse(
                sha256 = it.sha256,
                md5 = it.md5,
                sha1 = it.sha1,
                report = FileScanResponse(
                    date = it.date,
                    filename = it.filename,
                    results = reports
                )
            )
        }
}