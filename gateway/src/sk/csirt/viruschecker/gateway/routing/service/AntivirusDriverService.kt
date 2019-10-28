package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
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

    @ExperimentalCoroutinesApi
    suspend fun <T> CoroutineScope.multiDriverRequestChannel(
        block: suspend (driverUrl: String, client: HttpClient) -> T
    ): ReceiveChannel<MultiDriverResponse<Result<T>>> = produce<MultiDriverResponse<Result<T>>> {
        drivers.map { driverUrl ->
            async(IO) {
                logger.info { "Requesting from $driverUrl" }
                val result = runCatching { block(driverUrl, client) }
                    .onFailure {
                        logger.error { "Failed http post to $driverUrl, cause is \n${it.stackTrace}" }
                    }.onSuccess {
                        logger.info { "Retrieved report from $driverUrl: $it" }
                    }
                send(
                    MultiDriverResponse(
                        driverUrl,
                        result
                    )
                )
            }
        }.awaitAll()
    }

    data class MultiDriverResponse<R>(
        val driverUrl: String,
        val result: R
    )
}