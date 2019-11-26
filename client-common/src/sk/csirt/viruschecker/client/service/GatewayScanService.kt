package sk.csirt.viruschecker.client.service

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
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.HashResponse
import sk.csirt.viruschecker.routing.payload.ScanFileWebSocketParameters
import sk.csirt.viruschecker.utils.HostPort
import sk.csirt.viruschecker.utils.fromJson
import sk.csirt.viruschecker.utils.json
import java.io.File
import java.io.FileInputStream

data class ScanParameters(
    val fileToScan: File,
    val originalFilename: String,
    val useExternalDrivers: Boolean
)

class GatewayScanService(
    private val gatewayUrl: String,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger {}
    private val gatewayHostPort: HostPort = HostPort.fromUrlWithPort(gatewayUrl)

    suspend fun scanFile(params: ScanParameters): FileHashScanResponse =
        client.post("$gatewayUrl${GatewayRoutes.multiScanFile}") {
            this.body = MultiPartFormDataContent(
                listOf(
                    PartData.FileItem(
                        partHeaders = Headers.build {
                            this[HttpHeaders.ContentDisposition] =
                                ContentDisposition.File.withParameter(
                                    "filename",
                                    params.originalFilename
                                ).toString()
                        },
                        dispose = { },
                        provider = { FileInputStream(params.fileToScan).buffered().asInput() }
                    ),
                    PartData.FormItem(
                        value = params.useExternalDrivers.toString(),
                        dispose = { },
                        partHeaders = Headers.Empty
                    )
                )
            )
        }

    data class HashTriple(
        val md5: String,
        val sha1: String,
        val sha256: String
    )

    suspend fun scanFileWebSocket(
        params: ScanParameters,
        onHashReceived: (HashTriple) -> Unit,
        onReceived: (AntivirusReportResponse) -> Unit
    ) {
        logger.debug { "Initializing WebSocket connection to $gatewayUrl with params $params" }
        client.webSocket(
            // method = HttpMethod.Post,
            host = gatewayHostPort.host,
            port = gatewayHostPort.port,
            path = GatewayRoutes.multiScanFileWebSocket
        ) {
            logger.info { "Established WebSocket connection to $gatewayUrl with params $params" }

            val scanRequestParams = ScanFileWebSocketParameters(
                useExternalServices = params.useExternalDrivers,
                originalFilename = params.originalFilename
            )
            logger.debug { "Sending WebSocket text frame '$scanRequestParams'" }
            send(Frame.Text(scanRequestParams.json()))

            logger.debug { "Sending WebSocket binary frame" }
            send(
                Frame.Binary(
                    true,
                    params.fileToScan.readBytes()
                )
            )

            val (md5, sha1, sha256) = (incoming.receive() as Frame.Text).readText().fromJson<HashResponse>()

            onHashReceived(
                HashTriple(
                    md5 = md5,
                    sha1 = sha1,
                    sha256 = sha256
                )
            )

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val message = frame.readText()
                        logger.debug { "Receiving WebSocket text frame: $message" }
                        val antivirusResponse =
                            message.fromJson<AntivirusReportResponse>()
                        onReceived(antivirusResponse)
                    }
                    is Frame.Close -> {
                        logger.info { "WebSocket connection to $gatewayUrl closed" }
                        close(CloseReason(CloseReason.Codes.NORMAL, "WebSocket connection finished normally"))
                    }
                    else -> logger.debug { "Receiving not supported WebSocket frame" }
                }
            }
            logger.info { "WebSocket connection to $gatewayUrl finished" }
            close(CloseReason(CloseReason.Codes.NORMAL, "WebSocket connection finished normally"))
        }
    }

}


