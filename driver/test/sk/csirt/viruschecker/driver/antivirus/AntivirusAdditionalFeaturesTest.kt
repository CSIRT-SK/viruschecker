package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals

@FlowPreview
@ExperimentalCoroutinesApi
internal class AntivirusAdditionalFeaturesTest {

    private val antivirus = DummyAntivirus()

    private fun testFileScanParameters() = FileScanParameters(
        fileToScan = createTempFile(),
        externalServicesAllowed = true,
        originalFileName = "Neznam"
    )

    @Test
    fun `Clean file after scan test`()  {
        runBlocking {
            antivirus.scanFileAndClean(testFileScanParameters())
        }
    }

    @Test
    fun `Scan file using channel`() = runBlockingTest {
        val channel = antivirus.run { scanFileChannel(testFileScanParameters()) }
        assertEquals(1, channel.consumeAsFlow().count())
    }

    @Test
    fun `Clean file after scan using channel test`() {
//        runBlocking {
//            antivirus.run { scanFileChannelAndClean(testFileScanParameters()) }
//        }
    }
}