package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.coroutineScope
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import java.io.File
import java.io.FileInputStream

class MultiScanService(
    private val gatewayUrl: String,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    suspend fun scanFile(fileToScan: File): FileMultiScanResponse = coroutineScope {
        client.post<FileMultiScanResponse>("$gatewayUrl${GatewayRoutes.scanFile}") {
            this.body = MultiPartFormDataContent(listOf(
                PartData.FileItem(
                    partHeaders = Headers.build {
                        this[HttpHeaders.ContentDisposition] =
                            ContentDisposition.File.withParameter(
                                "filename",
                                fileToScan.name
                            ).toString()
                    },
                    dispose = { },
                    provider = { FileInputStream(fileToScan).asInput() }
                )
            ))
        }
    }
}