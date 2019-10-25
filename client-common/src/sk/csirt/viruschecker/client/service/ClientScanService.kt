package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.ws
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.PartData
import kotlinx.io.streams.asInput
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

    private val gatewayHost: String
    private val gatewayPort: Int

    init {
        val gatewayHostPort = gatewayUrl.split(":")
        gatewayHost = gatewayHostPort[0]
        gatewayPort = gatewayHostPort[1].toIntOrNull() ?: 0
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

    suspend fun scanFileWebSocket(
        params: ScanParameters,
        onReceived: (AntivirusReportResponse) -> Unit
    ) {
        client.ws(
            method = HttpMethod.Post,
            host = gatewayHost,
            port = gatewayPort,
            path = GatewayRoutes.multiScanFileWebSocket
        ) {
            if (params.useExternalDrivers) {
                send(Frame.Text("useExternalDrivers: true"))
            } else {
                send(Frame.Text("useExternalDrivers: false"))
            }
            send(Frame.Text(params.originalFilename))
            send(
                Frame.Binary(
                    true,
                    params.fileToScan.readBytes()
                )
            )
//            val md5 = (incoming.receive() as Frame.Text).readText()
//            val sha1 = (incoming.receive() as Frame.Text).readText()
//            val sha256 = (incoming.receive() as Frame.Text).readText()

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val antivirusResponse =
                        jsonConverter.fromJson<AntivirusReportResponse>(
                            frame.readText()
                        )
                    onReceived(antivirusResponse)
                }
            }
        }
    }

}


