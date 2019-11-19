package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import mu.KotlinLogging
import sk.csirt.viruschecker.utils.HostPort


abstract class AntivirusDriverService(
    private val drivers: List<String>,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    private val driverHostsPorts = drivers.map { HostPort.fromUrlWithPort(it) }

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
        block: suspend CoroutineScope.(
            hostPort: HostPort,
            client: HttpClient,
            resultChannel: SendChannel<T>
        ) -> Unit
    ): ReceiveChannel<MultiDriverResponse<Result<T>>> = produce<MultiDriverResponse<Result<T>>> {
        driverHostsPorts.map { driverUrl ->
            async(IO) {
                logger.info { "Requesting $driverUrl" }
//                val driverChannel = Channel<T>()
                runCatching { produce { block(driverUrl, client, this) } }
                    .onFailure {
                        logger.error { "Failed http post to $driverUrl, cause is \n${it.stackTrace}" }
                        send(
                            MultiDriverResponse(
                                driverUrl.toString(),
                                Result.failure(it)
                            )
                        )
                    }.onSuccess { driverChannel ->
                        logger.info { "Retrieved report from $driverUrl" }
                        for (result in driverChannel) {
                            send(
                                MultiDriverResponse(
                                    driverUrl.toString(),
                                    Result.success(result)
                                )
                            )
                        }
                    }
            }
        }.awaitAll()
    }

    data class MultiDriverResponse<R>(
        val driverUrl: String,
        val result: R
    )
}