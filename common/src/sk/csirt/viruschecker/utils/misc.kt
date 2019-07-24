package sk.csirt.viruschecker.utils

import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

val tempDirectory = System.getProperty("java.io.tmpdir")

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
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
        return@withContext bytesCopied
    }
}

suspend fun <T> Iterable<Deferred<T>>.await() = map { it.await() }

fun Iterable<String>.cleanCommentsAndEmptyLines() =
    asSequence()
        .filterNot { it.startsWith("#") }
        .filterNot { it.isBlank() }
        .map { line ->
            line.indexOfFirst { '#' == it }
                .takeIf { it > 0 }
                ?.let { line.substring(0, it) } ?: line
        }.toList()
