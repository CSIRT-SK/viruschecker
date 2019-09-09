package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging

abstract class AntivirusDriverService(
    private val drivers: List<String>,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

        suspend fun <T> multiDriverRequest(
        block: suspend (driverUrl: String, client: HttpClient) -> T
    ): List<MultiDriverResponse<Result<T>>> = supervisorScope {
        drivers.map { driverUrl ->
            driverUrl to async(IO) {
                logger.info { "Requesting from $driverUrl" }
                block(driverUrl, client)
            }
        }.map { (driverUrl, deferredT) ->
            val result = runCatching { deferredT.await() }
                .onFailure {
                    logger.error { "Failed http post to $driverUrl, cause is \n${it.stackTrace}" }
                }.onSuccess {
                    logger.info { "Retrieved report from $driverUrl: $it" }
                }
            MultiDriverResponse(
                driverUrl = driverUrl,
                result = result
            )
        }
    }

    data class MultiDriverResponse<R>(
        val driverUrl: String,
        val result: R
    )
}