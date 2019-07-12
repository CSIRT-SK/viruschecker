package sk.csirt.viruschecker.gateway.service

import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

abstract class AntivirusDriverService(
    private val driverUrls: List<String>,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    suspend fun <T> multiDriverRequest(block: suspend (driverUrl: String, client: HttpClient) -> T)
            : List<T> = coroutineScope {
            driverUrls.map { driverUrl ->
                async {
                    logger.info { "Requesting from $driverUrl" }
                    block(driverUrl, client).also {
                        logger.info("Retrieved report from $driverUrl: $it")
                    }
                }
            }.map {
                it.await()
            }
        }
}