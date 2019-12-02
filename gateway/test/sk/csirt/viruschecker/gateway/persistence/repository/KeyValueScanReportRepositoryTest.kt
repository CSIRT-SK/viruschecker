package sk.csirt.viruschecker.gateway.persistence.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import sk.csirt.viruschecker.hash.HashAlgorithm
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntities
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntity1
import sk.csirt.viruschecker.gateway.persistence.testScanReportEntity2
import sk.csirt.viruschecker.hash.sha1
import sk.csirt.viruschecker.hash.sha256
import java.lang.Math.random
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class KeyValueScanReportRepositoryTest {

    private val mockStorage = ConcurrentHashMap(testScanReportEntities.associateBy { it.sha256 })

    private val repository = KeyValueScanReportRepository(mockStorage)

    @Test
    fun `Find all test`() {
        assertEquals(mockStorage.values.toSet(), runBlocking { repository.findAll() }.toSet())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Save test`() {
        runBlocking {
            val mockStorageCopy = ConcurrentHashMap(mockStorage)
            val repositoryCopy = KeyValueScanReportRepository(mockStorageCopy)
            val toSave = mockStorage.values.first().copy(
                sha256 = "AAAAAAARRGGGGHHHHHHHHH".sha256()
            )
            repositoryCopy.save(toSave)

            assertEquals(mockStorage.values.toSet() + toSave, mockStorageCopy.values.toSet())
        }
    }

    @Test
    fun `Find something by SHA-256 test`() {
            val hash1 = testScanReportEntity1.sha256
            val find1 = runBlocking { repository.findBySha256(hash1) }
            assertEquals(mockStorage[hash1], find1)

            val hash2 = testScanReportEntity2.sha256
            val find2 = runBlocking { repository.findBySha256(hash2) }
            assertEquals(mockStorage[hash2], find2)
    }

    @Test
    fun `Find nothing by SHA-256 test`() {
        runBlocking {
            val hash1 = random().sha256()
            val find1 = repository.findBySha256(hash1)
            assertNull(find1)
        }
    }
}