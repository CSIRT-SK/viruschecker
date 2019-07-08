package sk.csirt.viruschecker.gateway.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.service.payload.AntivirusDriverResponse
import sk.csirt.viruschecker.gateway.routing.payload.AntivirusResponse
import sk.csirt.viruschecker.gateway.routing.payload.FileScanResponse
import sk.csirt.viruschecker.gateway.routing.payload.ScannedFileStatus
import java.io.File
import java.io.FileInputStream
import java.time.Instant

class ScanService(
    private val driverUrls: List<String>,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    suspend fun scanFile(fileToScan: File): FileScanResponse = coroutineScope {
        val driverResponses = driverUrls.map { url ->
            async {
                client.post<AntivirusDriverResponse>("$url/scanFile") {
                    this.body = MultiPartFormDataContent(listOf(
                        PartData.FileItem(
                            partHeaders = Headers.build {
                                this[HttpHeaders.ContentDisposition] =
                                    ContentDisposition.File.withParameter("filename", fileToScan.name).toString()
                            },
                            dispose = {  },
                            provider = { FileInputStream(fileToScan).asInput() }
                        )
                    ))
                }.also {
                    logger.info("Retrieved report from $url: $it")
                }
            }
        }.map {
            it.await()
        }

        driverResponses.map {
            AntivirusResponse(
                status = ScannedFileStatus.valueOf(it.status.name),
                antivirus = it.antivirus,
                malwareDescription = it.malwareDescription
            )
        }.let {
            FileScanResponse(
                filename = driverResponses.firstOrNull()?.filename ?: "",
                reports = it,
                date = Instant.now(),
                status = it.maxBy { it.status }?.status
                    ?: ScannedFileStatus.NOT_AVAILABLE
            )
        }
    }
}