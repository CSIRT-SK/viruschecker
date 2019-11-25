package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.client.testScanResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class FileReporterTest {

    @Test
    fun saveReport() {
        val tempFile = createTempFile()
        FileReporter(tempFile).saveReport(testScanResponse)
        assertTrue { tempFile.exists() }

        val content = tempFile.readText()
        assertEquals(testScanResponse.toString(), content)
    }
}