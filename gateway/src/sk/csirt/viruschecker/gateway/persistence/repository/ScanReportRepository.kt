package sk.csirt.viruschecker.gateway.persistence.repository

import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import java.time.Instant

interface ScanReportRepository {
    suspend fun save(item: ScanReportEntity): ScanReportEntity

    suspend fun findBySha256(hash: String): ScanReportEntity?

    suspend fun findAll(): Iterable<ScanReportEntity>

    suspend fun findBetween(
        begin: Instant,
        end: Instant
    ): List<ScanReportEntity> =
        findAll().filter { it.date > begin && it.date < end }

    suspend fun findBy(searchWords: Iterable<String>): List<ScanReportEntity>{
        val lowerSearchWords = searchWords.map { it.toLowerCase() }
        return findAll()
            .filter { scanReport ->
                run {
                    val filenameLower = scanReport.filename.toLowerCase()
                    scanReport.md5 in lowerSearchWords
                            || scanReport.sha1 in lowerSearchWords
                            || scanReport.sha256 in lowerSearchWords
                            || scanReport.date.toString() in lowerSearchWords
                            || filenameLower.let { filename -> lowerSearchWords.any { it in filename } }
                            || filenameLower.let { filename -> lowerSearchWords.any { filename in it } }
                            || scanReport.reports.joinToString(", ").toLowerCase().let { reports -> lowerSearchWords.any { it in reports } }
                }
            }
    }
}