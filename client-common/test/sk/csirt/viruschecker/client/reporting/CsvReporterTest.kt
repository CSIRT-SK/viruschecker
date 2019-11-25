package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.client.localDateTime
import sk.csirt.viruschecker.client.testScanResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class CsvReporterTest {

    @Test
    fun saveReport() {
        val tempFile = createTempFile()
        CsvReporter(tempFile).saveReport(testScanResponse)
        assertTrue { tempFile.exists() }

        val content = tempFile.readText().trimIndent()
        val expected = """file,date,antivirus,status,malwareDescription,sha256,sha1,md5
eicar.txt,$localDateTime,ClamAV,INFECTED,Eicar-TEST-File,131F95C51CC819465FA1797F6CCACF9D494AAAFF46FA3EAC73AE63FFBDFD8267,CF8BD9DFDDFF007F75ADF4C2BE48005CEA317C62,69630E4574EC6798239B091CDA43DCA0
""".trimIndent()
        assertEquals(expected, content)
    }
}