package sk.csirt.viruschecker.gateway.persistence.repository

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import sk.csirt.viruschecker.gateway.persistence.AntivirusReportItems
import sk.csirt.viruschecker.gateway.persistence.Database
import sk.csirt.viruschecker.gateway.persistence.ScanReports
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntities
import sk.csirt.viruschecker.gateway.routing.utils.toJodaDateTime

internal class SqlScanReportRepositoryTest : ReportRepositoryTest() {
    override fun prepareRepository(): ScanReportRepository {
        val database = Database()
        val repository = SqlScanReportRepository(Database())
        runBlocking {
            deleteTestData(database)
            insertTestData(database)
        }
        return repository
    }

    private suspend fun deleteTestData(database: Database) {
        database.query {
            AntivirusReportItems.deleteAll()
            ScanReports.deleteAll()
        }
    }

    private suspend fun insertTestData(database: Database) {
        testScanReportEntities.forEach { item ->
            database.query {
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
            }
        }
    }
}