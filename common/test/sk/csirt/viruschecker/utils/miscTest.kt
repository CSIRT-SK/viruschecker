package sk.csirt.viruschecker.utils

import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MiscTest{

    @Test
    fun `Copy stream suspend`(){
        val content = "Šlizký šlimak še šliznul po šlizkej šline šlizkého šlimaka."
        val input = content.byteInputStream()
        val output = ByteArrayOutputStream()
        runBlocking { input.copyToSuspend(output) }
        val contentReturned = String(output.toByteArray())

        assertEquals(content, contentReturned)
    }

    @Test
    fun `Test filter comments denoted by '#' and empty lines`(){
        val okLine1 = "this.is=wanted"
        val okLine2 = "this will also pass"

        val input = listOf(
            "# this.is=comment",
            " ",
            okLine1,
            okLine2,
            "",
            "\t",
            "\n"
        )
        val expectedOutput = listOf(
            okLine1,
            okLine2
        )

        val output = input.filterPropertiesLines()

        assertEquals(expectedOutput, output)
    }

    @Test
    fun `Test byte array to temp file`(){
        val content = "Šlizký šlimak še šliznul po šlizkej šline šlizkého šlimaka.".toByteArray()
        val tempFile = content.toTempFile()
        assertTrue(tempFile.exists())

        val contentReturned = tempFile.readBytes()
        assertTrue(content contentEquals contentReturned)
    }
}