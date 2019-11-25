package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.client.testScanResponse
import kotlin.test.Test


internal class CommandLineReporterTest {

    @Test
    fun saveReport() {
        CommandLineReporter().saveReport(testScanResponse)
    }
}