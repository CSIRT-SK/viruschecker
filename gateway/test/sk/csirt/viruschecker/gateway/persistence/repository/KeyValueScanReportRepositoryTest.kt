package sk.csirt.viruschecker.gateway.persistence.repository

import kotlinx.coroutines.runBlocking
import sk.csirt.viruschecker.gateway.persistence.entity.AntivirusReportEntity
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.Test
import kotlin.test.assertEquals

internal class KeyValueScanReportRepositoryTest {

    private val database = mapOf(
        "sha256_0" to ScanReportEntity(
            sha256 = "sha256_0",
            sha1 = "sha1_0",
            md5 = "md5_0",
            filename = "file_0",
            date = Instant.now(),
            reports = listOf(
                AntivirusReportEntity(
                    antivirus = "av_0",
                    virusDatabaseVersion = "vdv_0",
                    malwareDescription = "mwd_0",
                    status = "OK"
                )
            )
        )
    ).let { ConcurrentHashMap(it) }

    private val repository = KeyValueScanReportRepository(database)

    @Test
    fun findAll() {
        assertEquals(database.values.toSet(), runBlocking { repository.findAll() }.toSet())
    }

    @Test
    fun save() {

    }

    @Test
    fun findBySha256() {
    }
}