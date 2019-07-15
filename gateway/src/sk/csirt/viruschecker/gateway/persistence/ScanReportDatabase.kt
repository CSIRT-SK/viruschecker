package sk.csirt.viruschecker.gateway.persistence

import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import java.io.File

class ScanReportDatabase(
    file: File,
    name: String,
    entries: Long,
    sampleKey: String,
    sampleValue: ScanReportEntity
) : Database<String, ScanReportEntity>(
    file = file,
    name = name,
    sampleKey = sampleKey,
    sampleValue = sampleValue,
    entries = entries
)