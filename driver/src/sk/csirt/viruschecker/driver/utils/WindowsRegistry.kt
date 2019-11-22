package sk.csirt.viruschecker.driver.utils

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import mu.KotlinLogging

class WindowsRegistry {
    private val logger = KotlinLogging.logger {}

    suspend fun read(path: String, key: String): String =
        withContext(IO) {
            ProcessBuilder(
                "reg",
                "query",
                path,
                "/v",
                key
            ).start()
                .inputStream
                .bufferedReader()
                .useLines { it.toList() }
                .also { logger.debug { "Registry $path\\$key loaded value is: $it" } }
                .firstOrNull { key in it }
                ?.split(" ")
                ?.last()
                ?: ""
        }
}