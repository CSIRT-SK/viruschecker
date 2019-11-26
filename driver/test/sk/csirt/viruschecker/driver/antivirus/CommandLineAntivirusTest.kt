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
internal abstract class CommandLineAntivirusTest
    : AntivirusTest {

    abstract fun mockFileScanOutputHealthy(filename: String): String
    abstract fun mockFileScanOutputInfected(filename: String): String
    abstract fun mockArchiveFileScanOutputHealthy(filename: String): String
    abstract fun mockArchiveFileScanOutputInfected(filename: String): String

    abstract fun antivirusFactory(
        command: RunProgramCommand,
        processRunner: ProcessRunner
    ): CommandLineAntivirus

    private suspend fun performMockedScan(
        isArchive: Boolean,
        mockedAntivirusOutput: (filename: String) -> String
    ): FileScanResult {
        val tempFile = createTempFile()
        val originalFileName = if (isArchive) "test-file-name.zip" else "test-file-name.txt"


        val mockedOutputLines = mockedAntivirusOutput(tempFile.name)
            .replace(RunProgramCommand.SCAN_FILE, tempFile.name)
            .split("\n")
        val processRunner = mockk<ProcessRunner>() {
            coEvery { runProcess(any()) } returns mockedOutputLines
        }

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
    override fun `Healthy file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(false) { mockFileScanOutputHealthy(it) }
        assertEquals(ScanStatusResult.OK, scanResult.scanReport.status)
    }

    @Test
    override fun `Infected file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(false) { mockFileScanOutputInfected(it) }
        assertEquals(ScanStatusResult.INFECTED, scanResult.scanReport.status)
    }

    @Test
    override fun `Healthy archive file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(true) { mockArchiveFileScanOutputHealthy(it) }
        assertEquals(ScanStatusResult.OK, scanResult.scanReport.status)
    }

    @Test
    override fun `Infected archive file scan test`() = runBlockingTest {
        val scanResult = performMockedScan(true) { mockArchiveFileScanOutputInfected(it) }
        assertEquals(ScanStatusResult.INFECTED, scanResult.scanReport.status)
    }

    @Test
    override fun `Get virus database version test`() = runBlockingTest {
        val scanResult = performMockedScan(false) { mockFileScanOutputHealthy(it) }
        assertTrue(scanResult.scanReport.reports.all { it.virusDatabaseVersion.isNotBlank() })
    }

}