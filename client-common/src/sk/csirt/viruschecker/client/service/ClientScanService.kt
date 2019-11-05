package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.ws
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.DEFAULT_PORT
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.PartData
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.utils.JsonConverter
import java.io.File
import java.io.FileInputStream

data class ScanParameters(
    val fileToScan: File,
    val originalFilename: String,
    val useExternalDrivers: Boolean
)

//data class ScanMetadataWebSocket(
//    val md5: String,
//    val sha1: String,
//    val sha256: String
//)

class ClientScanService(
    private val gatewayUrl: String,
    private val client: HttpClient,
    private val jsonConverter: JsonConverter
) {
    private val logger = KotlinLogging.logger {}
    private val gatewayHost: String
    private val gatewayPort: Int

    init {
        val gatewayHostPort = gatewayUrl.split(":")
        gatewayHost = gatewayHostPort[1].substring(2)
        gatewayPort = gatewayHostPort.getOrNull(2)?.toIntOrNull() ?: DEFAULT_PORT
    }

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
        client.ws(
            // method = HttpMethod.Post,
            host = gatewayHost,
            port = gatewayPort,
            path = GatewayRoutes.multiScanFileWebSocket
        ) {
            logger.info { "Established WebSocket connection to $gatewayUrl with params $params" }
            if (params.useExternalDrivers) {
                logger.debug { "Sending WebSocket text frame 'useExternalDrivers: true'" }
                send(Frame.Text("useExternalDrivers: true"))
            } else {
                logger.debug { "Sending WebSocket text frame 'useExternalDrivers: false'" }
                send(Frame.Text("useExternalDrivers: false"))
            }
            logger.debug { "Sending WebSocket text frame 'params.originalFilename'" }
            send(Frame.Text(params.originalFilename))
            logger.debug { "Sending WebSocket binary frame" }
            send(
                Frame.Binary(
                    true,
                    params.fileToScan.readBytes()
                )
            )
//            outgoing.close()

            val md5 = (incoming.receive() as Frame.Text).readText()
            val sha1 = (incoming.receive() as Frame.Text).readText()
            val sha256 = (incoming.receive() as Frame.Text).readText()

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
                            jsonConverter.fromJson<AntivirusReportResponse>(
                                message
                            )
                        onReceived(antivirusResponse)
                    }
                    else -> logger.debug { "Receiving not supported WebSocket frame" }
                }
            }
            logger.info { "Finished WebSocket connection to $gatewayUrl with params $params" }
        }
    }

}


