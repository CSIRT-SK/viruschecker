package sk.csirt.viruschecker.gateway.persistence.service

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.gateway.persistence.repository.ScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.service.converter.toFileHashScanResponse
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntities
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntity1
import sk.csirt.viruschecker.hash.sha256
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PersistentScanReportServiceTest {

    @Test
    fun `Save test`() {
        val mockRepository = mockk<ScanReportRepository> {
            coEvery { save(any()) } returnsArgument 0
        }
        val service = PersistentScanReportService(mockRepository)
        runBlocking { service.save(testScanReportEntity1.toFileHashScanResponse()) }
    }

    @Test
    fun `Find something by SHA-256 test`() {
        val mockRepository = mockk<ScanReportRepository> {
            coEvery { findBySha256(any()) } answers {
                testScanReportEntities.find {
                    it.sha256 == invocation.args[0]
                }
            }
        }
        val service = PersistentScanReportService(mockRepository)
        val result = runBlocking { service.findBySha256(testScanReportEntity1.sha256) }

        assertEquals(testScanReportEntity1.toFileHashScanResponse(), result)
    }

    @Test
    fun `Find nothing by SHA-256 test`() {
        val mockRepository = mockk<ScanReportRepository> {
            coEvery { findBySha256(any()) } returns null
        }
        val service = PersistentScanReportService(mockRepository)
        val result = runBlocking { service.findBySha256("Something, something, something DARK SIDE".sha256()) }

        assertNull(result)
    }

    @Test
    fun `Find all test`() {
        val mockRepository = mockk<ScanReportRepository> {
            coEvery { findAll() } returns testScanReportEntities
        }
        val service = PersistentScanReportService(mockRepository)
        val result = runBlocking { service.findAll() }

        assertEquals(
            testScanReportEntities.map { it.toFileHashScanResponse() }.toSet(),
            result.toSet()
        )
    }


    private fun executeTestFindBy(
        searchWords: Iterable<String>,
        shouldReturn: List<ScanReportEntity>) {
        val mockRepository = mockk<ScanReportRepository> {
            coEvery { findBy(any()) } returns shouldReturn
        }
        val service = PersistentScanReportService(mockRepository)
        val result = runBlocking {
            service.findBy(searchWords)
        }
        assertEquals(shouldReturn.count(), result.count())
        assertEquals(shouldReturn.map { it.toFileHashScanResponse() }.toSet(), result.toSet())
    }

    @Test
    fun `Find one by search word test`() {
        val searchWords = listOf(
            testScanReportEntities.first().filename.substring(5)
        )
        executeTestFindBy(searchWords, listOf(testScanReportEntities.first()))
    }

    @Test
    fun `Find many by search word test`() {
        val searchWords = listOf(
            testScanReportEntities.first().filename.substring(5),
            testScanReportEntities.last().reports.first().antivirus.substring(1)
        )
        executeTestFindBy(searchWords, testScanReportEntities)
    }

    @Test
    fun `Find none by search word test`() {
        val searchWords = listOf(UUID.randomUUID().toString())
        executeTestFindBy(searchWords, emptyList())
    }
}
