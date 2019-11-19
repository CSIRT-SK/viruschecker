package sk.csirt.viruschecker.driver.antivirus

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import sk.csirt.viruschecker.driver.utils.ProcessRunner
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@Ignore
internal abstract class CommandLineAntivirusTest {

    abstract val mockFileScanOutputHealthy: List<String>
    abstract val mockFileScanOutputInfected: List<String>
    abstract val mockArchiveFileScanOutputHealthy: List<String>
    abstract val mockArchiveFileScanOutputInfected: List<String>

    abstract fun antivirusFactory(
        command: RunProgramCommand,
        processRunner: ProcessRunner
    ): CommandLineAntivirus

    private suspend fun performMockedScan(mockedAntivirusOutput: List<String>): FileScanResult {
        val processRunner = mockk<ProcessRunner>()

        coEvery { processRunner.runProcess(any()) } returns mockedAntivirusOutput
        val antivirus = antivirusFactory(
            command = RunProgramCommand(""),
            processRunner = processRunner
        )

        val tempFile = createTempFile()
        val originalFileName = "test-file-name.txt"
        val scanParameters = FileScanParameters(
            fileToScan = tempFile,
            originalFileName = originalFileName,
            externalServicesAllowed = true
        )

        val scanResult = antivirus.scanFile(scanParameters)

        coVerify { processRunner.runProcess(any()) }
        assertEquals(originalFileName, scanResult.filename)
        assertTrue(scanResult.scanReport.reports.isNotEmpty())
        assertTrue(scanResult.scanReport.reports.all { it.antivirusName.isNotBlank() })
        assertTrue(scanResult.scanReport.reports.any { it.virusDatabaseVersion.isNotBlank() })

        return antivirus.scanFile(scanParameters)
    }

    @Test
    fun `Healthy file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(mockArchiveFileScanOutputHealthy)
        assertEquals(ScanStatusResult.OK, scanResult.scanReport.status)
    }

    @Test
    fun `Infected file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(mockArchiveFileScanOutputHealthy)
        assertEquals(ScanStatusResult.INFECTED, scanResult.scanReport.status)
    }

    @Test
    fun `Healthy archive file scan test`() {

    }

    @Test
    fun `Infected archive file scan test`() {

    }

    @Test
    fun `Cleaning file test`() {

    }
}