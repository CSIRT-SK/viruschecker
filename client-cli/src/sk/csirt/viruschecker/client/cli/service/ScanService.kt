package sk.csirt.viruschecker.client.cli.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.runBlocking
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.client.cli.payload.FileScanResponse
import java.io.File
import java.io.FileInputStream

class ScanService(
    private val gatewayUrl: String,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    fun scanFile(fileToScan: File) = runBlocking {
        client.post<FileScanResponse>("$gatewayUrl/scanFile") {
            this.body = MultiPartFormDataContent(
                listOf(
                    PartData.FileItem(
                        partHeaders = Headers.build {
                            this[HttpHeaders.ContentDisposition] =
                                ContentDisposition.File.withParameter("filename", fileToScan.name).toString()
                        },
                        dispose = {  },
                        provider = { FileInputStream(fileToScan).asInput() }
                    )
                )
            )
        }.also {
            logger.info("Retrieved report: $it")
        }
    }

}