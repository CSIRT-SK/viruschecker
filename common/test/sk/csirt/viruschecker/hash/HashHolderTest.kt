package sk.csirt.viruschecker.hash

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class HashHolderTest {

    private val content = "Šlizký šlimak še šliznul po šlizkej šline šlizkého šlimaka."

    private fun testHash(
        expectedHash: String,
        hasher: suspend String.() -> String) {
        val hash = runBlocking { content.hasher() }
        assertEquals(expectedHash.toLowerCase(), hash.toLowerCase())
    }

    @Test
    fun `Test MD5`() {
        testHash("7BBB106021FE60CE2FDAFF5B5701F732") { md5() }
    }

    @Test
    fun `Test SHA1`() {
        testHash("5D2C22E04B2201BC9AC40B55B2D4FED7B572533E") { sha1() }
    }

    @Test
    fun `Test SHA256`() {
        testHash("69C91296B147A82C0226A3E281A4B831FC6B8B0ADE2455B26EE250A539FB2962") { sha256() }
    }
}