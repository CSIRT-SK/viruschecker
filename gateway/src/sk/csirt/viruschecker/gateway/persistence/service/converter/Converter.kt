package sk.csirt.viruschecker.gateway.persistence.service.converter

import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.FileScanResponse

internal fun ScanReportEntity.toFileHashScanResponse(): FileHashScanResponse {
    val reports = reports.map {
        AntivirusReportResponse(
            antivirus = it.antivirus,
            malwareDescription = it.malwareDescription,
            status = it.status,
            virusDatabaseVersion = it.virusDatabaseVersion
        )
    }
    return FileHashScanResponse(
        sha256 = sha256,
        md5 = md5,
        sha1 = sha1,
        report = FileScanResponse(
            date = date,
            filename = filename,
            results = reports
        )
    )
}