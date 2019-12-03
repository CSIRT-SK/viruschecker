package sk.csirt.viruschecker.utils

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.InputStream
import java.io.OutputStream

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024
): Long = withContext(IO) {
    val buffer = ByteArray(bufferSize)
    var bytesCopied = 0L
    var bytesAfterYield = 0L
    while (true) {
        val bytes = read(buffer).takeIf { it >= 0 } ?: break
        out.write(buffer, 0, bytes)
        if (bytesAfterYield >= yieldSize) {
            yield()
            bytesAfterYield %= yieldSize
        }
        bytesCopied += bytes
        bytesAfterYield += bytes
    }
    bytesCopied
}

fun Iterable<String>.filterPropertiesLines(): List<String> =
    asSequence()
        .filterNot { it.startsWith("#") }
        .filterNot { it.isBlank() }
        .map { line ->
            line.indexOfFirst { '#' == it }
                .takeIf { it > 0 }
                ?.let { line.substring(0, it) } ?: line
        }.toList()

fun ByteArray.toTempFile(): File {
    val tempFile = createTempFile()
    tempFile.writeBytes(this)
    return tempFile
}