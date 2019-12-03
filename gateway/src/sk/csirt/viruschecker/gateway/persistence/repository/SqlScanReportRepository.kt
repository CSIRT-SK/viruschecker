package sk.csirt.viruschecker.gateway.persistence.repository

import org.jetbrains.exposed.sql.*
import sk.csirt.viruschecker.gateway.persistence.AntivirusReportItems
import sk.csirt.viruschecker.gateway.persistence.Database
import sk.csirt.viruschecker.gateway.persistence.ScanReports
import sk.csirt.viruschecker.gateway.persistence.entity.AntivirusReportEntity
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.gateway.routing.utils.toJavaTimeInstant
import sk.csirt.viruschecker.gateway.routing.utils.toJodaDateTime
import sk.csirt.viruschecker.gateway.routing.utils.toOp
import java.time.Instant

class SqlScanReportRepository(private val db: Database) : ScanReportRepository {

    override suspend fun findBySha256(hash: String): ScanReportEntity? = db.query {
        val scanReport = ScanReports
            .select { ScanReports.sha256 eq hash }
            .limit(1)
            .firstOrNull()
        scanReport?.let { scanReportEntity(it) }
    }

    override suspend fun findAll(): Iterable<ScanReportEntity> = db.query {
        ScanReports
            .selectAll()
            .map { scanReportEntity(it) }
    }

    override suspend fun save(item: ScanReportEntity): ScanReportEntity = db.query {
        val scanReportId = ScanReports.insertAndGetId { row ->
            row[sha1] = item.sha1
            row[sha256] = item.sha256
            row[date] = item.date.toJodaDateTime()
            row[filename] = item.filename
            row[md5] = item.md5
        }
        AntivirusReportItems.batchInsert(item.reports) { reportItem ->
            this[AntivirusReportItems.scanReport] = scanReportId
            this[AntivirusReportItems.antivirus] = reportItem.antivirus
            this[AntivirusReportItems.malwareDescription] = reportItem.malwareDescription
            this[AntivirusReportItems.status] = reportItem.status
            this[AntivirusReportItems.virusDatabaseVersion] = reportItem.virusDatabaseVersion
        }
    }.let {
        findBySha256(item.sha256)!!
    }

    override suspend fun findBetween(begin: Instant, end: Instant): List<ScanReportEntity> = db.query {
        ScanReports
            .select { ScanReports.date.between(begin.toJodaDateTime(), end.toJodaDateTime()) }
            .map { scanReportEntity(it) }
    }

    override suspend fun findBy(searchWords: Iterable<String>): List<ScanReportEntity> {
        val lowerSearchWords = searchWords.map { it.toLowerCase() }
        return db.query {
            ScanReports
                .select {
                    OrOp(
                        listOf(
                            ScanReports.sha256 inList lowerSearchWords,
                            ScanReports.sha1 inList lowerSearchWords,
                            ScanReports.md5 inList lowerSearchWords,
                            ScanReports.date.castTo<String>(VarCharColumnType()) inList lowerSearchWords,
                            exists(
                                AntivirusReportItems.select {
                                    ScanReports.id eq AntivirusReportItems.scanReport and
                                            OrOp(
                                                listOf(
                                                    AntivirusReportItems.virusDatabaseVersion inList lowerSearchWords,
                                                    AntivirusReportItems.status.castTo<String>(VarCharColumnType()) inList lowerSearchWords,
                                                    AntivirusReportItems.antivirus.lowerCase() inList lowerSearchWords
                                                ) + lowerSearchWords.flatMap { word ->
                                                    listOf(
                                                        AntivirusReportItems.malwareDescription.lowerCase() like "%$word%",
                                                        LikeOp(
                                                            word.toOp(),
                                                            concat(
                                                                "%".toOp(),
                                                                AntivirusReportItems.malwareDescription,
                                                                "%".toOp()
                                                            )
                                                        )
                                                    )
                                                }
                                            )
                                }
                            )

                        ) + lowerSearchWords.flatMap { word ->
                            listOf(
                                ScanReports.filename.lowerCase() like "%$word%",
                                LikeOp(
                                    word.toOp(),
                                    concat(
                                        "%".toOp(),
                                        ScanReports.filename,
                                        "%".toOp()
                                    )
                                )
                            )

                        }
                    )
                }.map { scanReportEntity(it) }
        }
    }

    private fun ResultRow.toAntivirusReportEntity() = AntivirusReportEntity(
        antivirus = this[AntivirusReportItems.antivirus],
        status = this[AntivirusReportItems.status],
        malwareDescription = this[AntivirusReportItems.malwareDescription],
        virusDatabaseVersion = this[AntivirusReportItems.virusDatabaseVersion]
    )

    private fun Transaction.scanReportEntity(scanReport: ResultRow): ScanReportEntity {
        val scanReportId = scanReport[ScanReports.id]
        val antivirusReports = AntivirusReportItems.select {
            AntivirusReportItems.scanReport eq scanReportId
        }.map { it.toAntivirusReportEntity() }
        return ScanReportEntity(
            sha256 = scanReport[ScanReports.sha256],
            sha1 = scanReport[ScanReports.sha1],
            md5 = scanReport[ScanReports.md5],
            date = scanReport[ScanReports.date].toJavaTimeInstant(),
            filename = scanReport[ScanReports.filename],
            reports = antivirusReports
        )
    }
}