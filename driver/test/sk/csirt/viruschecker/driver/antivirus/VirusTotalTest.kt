package sk.csirt.viruschecker.driver.antivirus

import com.kanishka.virustotal.dto.FileScanReport
import com.kanishka.virustotal.dto.VirusScanInfo
import com.kanishka.virustotalv2.VirustotalPublicV2Impl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
internal class VirusTotalTest : AntivirusTest {
    private val testApiKey = "test-api-key"
    private val testFileName = "test-file-name.txt"

    @Test
    override fun `Healthy file scan test`() {
        val originalFileName = "test-file-name.txt"

        val mockedVirusTotalApiImplementation = mockk<VirustotalPublicV2Impl>() {
            every { getScanReport(any()) } returns null
        }
        val virusTotal = VirusTotal(
            apiKey = testApiKey,
            virusTotalApiImplementation = mockedVirusTotalApiImplementation
        )

        val tempFile = createTempFile()

        val scanParameters = FileScanParameters(
            fileToScan = tempFile,
            originalFileName = originalFileName,
            externalServicesAllowed = true
        )
        val scanResult = runBlocking { virusTotal.scanFile(scanParameters) }
        assertEquals(originalFileName, scanResult.filename)
        assertEquals(ScanStatusResult.NOT_AVAILABLE, scanResult.scanReport.status)
    }

    @Test
    override fun `Infected file scan test`() {
        val scanResult = mockedInfectedFileScan()
        assertEquals(testFileName, scanResult.filename)
        assertTrue(scanResult.scanReport.reports.isNotEmpty())
        assertEquals(ScanStatusResult.INFECTED, scanResult.scanReport.status)
        assertTrue(scanResult.scanReport.reports.all { it.antivirusName.isNotBlank() })
    }

    private fun mockedInfectedFileScan(): FileScanResult {
        val mockedVirusTotalApiImplementation = mockk<VirustotalPublicV2Impl>() {
            every<FileScanReport?> { getScanReport(any()) } returns FileScanReport().apply {
                md5 = "1d9ab62fa8be2e5934f7a192dd4728a2"
                sha1 = "7369a8d48546e3be64ff4c3e0655deba01ba22b8"
                sha256 = "a9f34a520f3fad3da9f961e769c6fce053975212aa38cbb7777f74f53ffeb0ce"
                scans = mapOf(
                    "HelluvaAV" to VirusScanInfo().apply {
                        isDetected = true
                        version = "1.1.1701"
                        result = "Skynet"
                        verboseMessage = "Scan finished. Run for your lives!!!"
                    },
                    "iAntivirus" to VirusScanInfo().apply {
                        isDetected = true
                        version = "2.2.74656"
                        result = "Model T800-101"
                        verboseMessage = "Scan finished. May the <insert-your-god> have mercy on your soul."
                    }
                )
            }
        }
        val virusTotal = VirusTotal(
            apiKey = testApiKey,
            virusTotalApiImplementation = mockedVirusTotalApiImplementation
        )
        val tempFile = createTempFile()
        val scanParameters = FileScanParameters(
            fileToScan = tempFile,
            originalFileName = testFileName,
            externalServicesAllowed = true
        )
        return runBlocking{ virusTotal.scanFile(scanParameters) }
    }

    @Test
    override fun `Healthy archive file scan test`() {
        `Healthy file scan test`()
    }

    @Test
    override fun `Infected archive file scan test`() {
        `Infected file scan test`()
    }

    @Test
    override fun `Get virus database version test`() {
        val report = mockedInfectedFileScan()
        assertTrue(report.scanReport.reports.first().virusDatabaseVersion.isNotBlank())
    }

    @Test
    fun `Handle not allowed external services scenario test`() {
        val originalFileName = "does-not-matter"
        val scanParameters = FileScanParameters(
            fileToScan = File(originalFileName),
            originalFileName = originalFileName,
            externalServicesAllowed = false
        )
        val mockedVirusTotalApiImplementation = mockk<VirustotalPublicV2Impl>() {
            every { getScanReport(any()) } returns null
        }
        val virusTotal = VirusTotal(
            apiKey = testApiKey,
            virusTotalApiImplementation = mockedVirusTotalApiImplementation
        )
        val scanResult = runBlocking { virusTotal.scanFile(scanParameters) }
        assertEquals(originalFileName, scanResult.filename)
        assertEquals(ScanStatusResult.SCAN_REFUSED, scanResult.scanReport.status)
    }

}