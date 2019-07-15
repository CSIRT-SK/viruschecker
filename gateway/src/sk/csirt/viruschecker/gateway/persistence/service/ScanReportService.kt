package sk.csirt.viruschecker.gateway.persistence.service

import sk.csirt.viruschecker.gateway.persistence.entity.AntivirusReportEntity
import sk.csirt.viruschecker.routing.payload.AntivirusScanResponse
import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import sk.csirt.viruschecker.routing.payload.ScannedFileStatus
import sk.csirt.viruschecker.gateway.persistence.repository.ScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity

class ScanReportService(private val reportRepository: ScanReportRepository) {
    internal fun save(response: FileMultiScanResponse) {
                ScanReportEntity(
            date = response.date,
            filename = response.filename,
            sha256 = response.sha256,
            otherHashes = response.otherHashes.filterNot { it.algorithm == "SHA-256" },
            reports = response.reports.map {
                AntivirusReportEntity(
                    antivirus = it.antivirus,
                    status = it.status.name,
                    malwareDescription = it.malwareDescription
                )
            }
        ).also { reportRepository.save(it) }
    }

    fun findBySha256(hash: String): FileMultiScanResponse? = reportRepository.findBySha256(hash)?.let {
        val reports = it.reports.map {
            AntivirusScanResponse(
                antivirus = it.antivirus,
                malwareDescription = it.malwareDescription,
                status = ScannedFileStatus.valueOf(it.status)
            )
        }
        FileMultiScanResponse(
            date = it.date,
            filename = it.filename,
            reports = reports,
            otherHashes = it.otherHashes,
            sha256 = it.sha256,
            status = reports.maxBy { it.status }?.status
                ?: ScannedFileStatus.NOT_AVAILABLE
        )
    }
}