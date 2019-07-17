package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging

abstract class AntivirusDriverService(
    private val driverUrls: List<String>,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    suspend fun <T> multiDriverRequest(
        block: suspend (driverUrl: String, client: HttpClient) -> T
    ): List<MultiDriverResponse<Result<T>>> = supervisorScope {
        driverUrls.map { driverUrl ->
            driverUrl to async {
                logger.info { "Requesting from $driverUrl" }
                block(driverUrl, client)
            }
        }.map { (driverUrl, deferredT) ->
            val result = runCatching { deferredT.await() }
                .onFailure {
                    logger.error { "Failed http post to $driverUrl, cause is \n${it.message}" }
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