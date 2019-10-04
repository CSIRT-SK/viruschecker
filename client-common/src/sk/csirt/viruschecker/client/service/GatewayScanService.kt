package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.io.streams.asInput
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
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
                        provider = { FileInputStream(params.fileToScan).asInput() }
                    ),
                    PartData.FormItem(
                        value = params.useExternalDrivers.toString(),
                        dispose = { },
                        partHeaders = Headers.Empty
                    )
                )
            )
        }
}


