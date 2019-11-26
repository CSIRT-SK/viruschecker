package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
internal class ComposedAntivirusTest {

    private val antiviruses = listOf(
        DummyAntivirus(),
        DummyAntivirus()
    )
    private val composedAntivirus = ComposedAntivirus(
        antiviruses
    )

    @Test
    fun `Scan file test`() {
        val tempFile = createTempFile()

        val scanParameters = FileScanParameters(
            fileToScan = tempFile,
            originalFileName = "eicar.txt",
            externalServicesAllowed = true
        )
        val scanResult = runBlocking { composedAntivirus.scanFile(scanParameters) }
        assertTrue(
            antiviruses.size <= scanResult.scanReport.reports.size
        )
        assertEquals(ScanStatusResult.INFECTED, scanResult.scanReport.status)
    }

    @Test
    fun `Scan file channel test`() {
        val tempFile = createTempFile()

        val scanParameters = FileScanParameters(
            fileToScan = tempFile,
            originalFileName = "eicar.txt",
            externalServicesAllowed = true
        )
        val scanResult = runBlocking { composedAntivirus.scanFile(scanParameters) }
        assertTrue(
            antiviruses.size <= scanResult.scanReport.reports.size
        )
        assertEquals(ScanStatusResult.INFECTED, scanResult.scanReport.status)
    }

}