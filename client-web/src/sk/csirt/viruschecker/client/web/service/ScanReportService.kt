package sk.csirt.viruschecker.client.web.service

import sk.csirt.viruschecker.client.payload.AntivirusScanResponse
import sk.csirt.viruschecker.client.payload.FileMultiScanResponse
import sk.csirt.viruschecker.client.payload.ScannedFileStatus
import sk.csirt.viruschecker.client.web.repository.ScanReportRepository
import sk.csirt.viruschecker.client.web.repository.entity.AntivirusReportEntity
import sk.csirt.viruschecker.client.web.repository.entity.ScanReportEntity
import java.util.*

class ScanReportService(private val reportRepository: ScanReportRepository) {
    fun save(response: FileMultiScanResponse, id: String = UUID.randomUUID().toString()) {
        ScanReportEntity(
            id = id,
            date = response.date,
            filename = response.filename,
            fileHashes = response.fileHashes,
            reports = response.reports.map {
                AntivirusReportEntity(
                    antivirus = it.antivirus,
                    status = it.status.name,
                    malwareDescription = it.malwareDescription
                )
            }
        ).also { reportRepository.save(it) }
    }

    fun findById(id: String): FileMultiScanResponse? = reportRepository.find(id)?.let {
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
            fileHashes = it.fileHashes,
            status = reports.maxBy { it.status }?.status ?: ScannedFileStatus.NOT_AVAILABLE
        )
    }
}