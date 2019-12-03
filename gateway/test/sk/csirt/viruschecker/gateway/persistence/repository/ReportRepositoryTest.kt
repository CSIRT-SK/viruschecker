package sk.csirt.viruschecker.gateway.persistence.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntities
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntity1
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntity2
import sk.csirt.viruschecker.hash.md5
import sk.csirt.viruschecker.hash.sha1
import sk.csirt.viruschecker.hash.sha256
import java.time.Duration
import java.time.Instant
import kotlin.test.*

internal abstract class ReportRepositoryTest {

    lateinit var repository: ScanReportRepository

    @BeforeTest
    fun init(){
        repository = prepareRepository()
    }

    protected abstract fun prepareRepository(): ScanReportRepository

    @Test
    fun `Find all test`() {
        assertEquals(testScanReportEntities.toSet(), runBlocking { repository.findAll() }.toSet())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Save test`() {
        runBlocking {
            val content = "AAAAAAARRGGGGHHHHHHHHH"
            val toSave = testScanReportEntities.first().copy(
                sha256 = content.sha256(),
                sha1 = content.sha1(),
                md5 = content.md5()
            )
            repository.save(toSave)

            assertEquals(testScanReportEntities.toSet() + toSave, repository.findAll().toSet())
        }
    }

    @Test
    fun `Find something by SHA-256 test`() {
        val hash1 = testScanReportEntity1.sha256
        val find1 = runBlocking { repository.findBySha256(hash1) }
        assertEquals(testScanReportEntity1, find1)

        val hash2 = testScanReportEntity2.sha256
        val find2 = runBlocking { repository.findBySha256(hash2) }
        assertEquals(testScanReportEntity2, find2)
    }

    @Test
    fun `Find nothing by SHA-256 test`() {
        val hash = Math.random().sha256()
        val find = runBlocking { repository.findBySha256(hash) }
        assertNull(find)
    }

    @Test
    fun `Find one by search words test`() {
        val searchWords = listOf(
            testScanReportEntity1.filename.let { it.substring(0, it.length / 2) },
            testScanReportEntity1.md5
        )
        val find = runBlocking { repository.findBy(searchWords) }
        assertEquals(1, find.size)
    }

    @Test
    fun `Find many by search words test`() {
        val searchWords = listOf(
            testScanReportEntity1.filename.let { it.substring(0, it.length / 2) },
            testScanReportEntity2.md5
        )
        val find = runBlocking { repository.findBy(searchWords) }
        assertEquals(2, find.size)
    }

    @Test
    fun `Find none by search words test`() {
        val searchWords = listOf("YouShallNotPass")
        val find = runBlocking { repository.findBy(searchWords) }
        assertTrue(find.isEmpty())
    }

    @Test
    fun `Find something between dates test`() {
        val dateFrom = Instant.now() - Duration.ofDays(1)
        val dateTo = Instant.now()
        val find = runBlocking { repository.findBetween(dateFrom, dateTo) }
        assertEquals(testScanReportEntities.toSet(), find.toSet())
    }

    @Test
    fun `Find nothing between dates test`() {
        val dateFrom = Instant.now() - Duration.ofDays(2)
        val dateTo = Instant.now() - Duration.ofDays(1)
        val find = runBlocking { repository.findBetween(dateFrom, dateTo) }
        assertTrue(find.toList().isEmpty())
    }
}