package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.PartData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.hash.md5
import sk.csirt.viruschecker.hash.sha1
import sk.csirt.viruschecker.hash.sha256
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.*
import sk.csirt.viruschecker.utils.fromJson
import sk.csirt.viruschecker.utils.json
import java.io.FileInputStream
import java.time.Instant

@ExperimentalCoroutinesApi
class DefaultDriverScanService(
    drivers: List<String>,
    client: HttpClient
) : AntivirusDriverService(drivers, client),
    FileScanService {

    private val logger = KotlinLogging.logger { }

    override suspend fun scanFile(scanParams: ScanParameters): FileHashScanResponse =
        coroutineScope {
            val (fileToScan, originalFileName) = scanParams
            logger.info { "Computing hashes for $fileToScan" }
            val sha256Deferred = async { fileToScan.sha256() }
            val sha1Deferred = async { fileToScan.sha1() }
            val md5Deferred = async { fileToScan.md5() }

            multiDriverRequest { driverUrl, client ->
                client.post<FileScanResponse>("$driverUrl${DriverRoutes.scanFile}") {
                    this.body = MultiPartFormDataContent(listOf(
                        PartData.FormItem(
                            value = scanParams.useExternalDrivers.toString(),
                            dispose = { },
                            partHeaders = Headers.Empty
                        ),
                        PartData.FileItem(
                            partHeaders = Headers.build {
                                this[HttpHeaders.ContentDisposition] =
                                    ContentDisposition.File.withParameter(
                                        "filename",
                                        originalFileName
                                    ).toString()
                            },
                            dispose = { },
                            provider = { FileInputStream(fileToScan).asInput() }
                        )
                    ))
                }
            }.map { (driverUrl, result) ->
                result.getOrDefault(
                    FileScanResponse(
                        date = Instant.now(),
                        filename = originalFileName,
                        results = listOf(
                            AntivirusReportResponse(
                                antivirus = "Unknown",
                                malwareDescription = "Connection to $driverUrl was unsuccessful.",
                                status = ScanStatus.NOT_AVAILABLE,
                                virusDatabaseVersion = ""
                            )
                        )
                    )
                )
            }.flatMap {
                it.results
            }.filterNot {
                it.status == ScanStatus.SCAN_REFUSED
            }.let { antivirusResponses ->
                FileHashScanResponse(
                    report = FileScanResponse(
                        date = Instant.now(),
                        filename = originalFileName,
                        results = antivirusResponses.sortedBy { it.antivirus }
                    ),
                    md5 = md5Deferred.await(),
                    sha1 = sha1Deferred.await(),
                    sha256 = sha256Deferred.await()
                )
            }
        }

    override suspend fun CoroutineScope.scanFileChannel(scanParams: ScanParameters): FileHashScanChannel {
        val (fileToScan, originalFilename, useExternalDrivers) = scanParams
        logger.info { "Computing hashes for $fileToScan" }
        val sha256Deferred = async { fileToScan.sha256() }
        val sha1Deferred = async { fileToScan.sha1() }
        val md5Deferred = async { fileToScan.md5() }

        val driverChannel =
            multiDriverRequestChannel<AntivirusReportResponse> { driverHostPort, client, resultChannel ->
                client.webSocket(
                    host = driverHostPort.host,
                    port = driverHostPort.port,
                    path = DriverRoutes.scanFileWebSocket
                ) {
                    logger.info {
                        "Established WebSocket connection to $driverHostPort with params $scanParams"
                    }
                    val scanRequestParams = ScanFileWebSocketParameters(
                        useExternalServices = useExternalDrivers,
                        originalFilename = originalFilename
                    )
                    logger.debug { "Sending WebSocket text frame '$scanRequestParams'" }
                    send(Frame.Text(scanRequestParams.json()))

                    logger.debug { "Sending WebSocket binary frame" }
                    send(
                        Frame.Binary(
                            true,
                            scanParams.fileToScan.readBytes()
                        )
                    )
                    for (scanResponseFrame in incoming) {
                        when (scanResponseFrame) {
                            is Frame.Text -> {
                                val message = scanResponseFrame.readText()
                                logger.debug { "Receiving WebSocket text frame: $message" }
                                val scanResponse = message.fromJson<AntivirusReportResponse>()
                                resultChannel.send(scanResponse)
                            }
                            is Frame.Close -> {
                                logger.info { "WebSocket connection to $driverHostPort closed" }
                                close(CloseReason(CloseReason.Codes.NORMAL, "WebSocket connection finished normally"))
                            }
                            else -> logger.debug { "Receiving not supported WebSocket frame" }
                        }
                    }
                    logger.info { "WebSocket connection to $driverHostPort finished" }
                    close(CloseReason(CloseReason.Codes.NORMAL, "WebSocket connection finished normally"))
                }

            }

        val receiveChannel = produce<AntivirusReportResponse> {
            for ((driverUrl, result) in driverChannel) {
                result.getOrDefault(
                    AntivirusReportResponse(
                        antivirus = "Unknown",
                        malwareDescription = "Connection to $driverUrl was unsuccessful.",
                        status = ScanStatus.NOT_AVAILABLE,
                        virusDatabaseVersion = ""
                    )
                ).let { antivirusReportResponse ->
                    logger.debug { "Sending response via WebSocket: $antivirusReportResponse" }
                    send(antivirusReportResponse)
                }
            }
        }

        return FileHashScanChannel(
            md5 = md5Deferred.await(),
            sha1 = sha1Deferred.await(),
            sha256 = sha256Deferred.await(),
            reportChannel = FileScanChannel(
                date = Instant.now(),
                filename = originalFilename,
                resultChannel = receiveChannel
            )
        )
    }
}