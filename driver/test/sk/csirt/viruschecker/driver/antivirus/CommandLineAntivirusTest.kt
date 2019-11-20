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

    abstract val mockFileScanOutputHealthy: String
    abstract val mockFileScanOutputInfected: String
    abstract val mockArchiveFileScanOutputHealthy: String
    abstract val mockArchiveFileScanOutputInfected: String

    abstract fun antivirusFactory(
        command: RunProgramCommand,
        processRunner: ProcessRunner
    ): CommandLineAntivirus

    private suspend fun performMockedScan(
        mockedAntivirusOutput: String,
        isArchive: Boolean
    ): FileScanResult {
        val tempFile = createTempFile()
        val originalFileName = if(isArchive) "test-file-name.zip" else "test-file-name.txt"


        val processRunner = mockk<ProcessRunner>()
        val mockedOutputLines = mockedAntivirusOutput
            .replace(RunProgramCommand.SCAN_FILE, tempFile.name)
            .split("\n")
        coEvery { processRunner.runProcess(any()) } returns mockedOutputLines

        val antivirus = antivirusFactory(
            command = RunProgramCommand(""),
            processRunner = processRunner
        )
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

        return antivirus.scanFile(scanParameters)
    }

    @Test
    fun `Healthy file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(mockFileScanOutputHealthy, false)
        assertEquals(ScanStatusResult.OK, scanResult.scanReport.status)
    }

    @Test
    fun `Infected file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(mockFileScanOutputInfected, false)
        assertEquals(ScanStatusResult.INFECTED, scanResult.scanReport.status)
    }

    @Test
    fun `Healthy archive file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(mockArchiveFileScanOutputHealthy, true)
        assertEquals(ScanStatusResult.OK, scanResult.scanReport.status)
    }

    @Test
    fun `Infected archive file scan test`() = runBlockingTest{
        val scanResult = performMockedScan(mockArchiveFileScanOutputInfected, true)
        assertEquals(ScanStatusResult.INFECTED, scanResult.scanReport.status)
    }

    @Test
    fun `Test getting database version`() = runBlockingTest {
        val scanResult = performMockedScan(mockFileScanOutputHealthy, false)
        assertTrue(scanResult.scanReport.reports.all { it.virusDatabaseVersion.isNotBlank() })
    }
}