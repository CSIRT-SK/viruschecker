package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.client.payload.AntivirusScanResponse
import sk.csirt.viruschecker.client.payload.FileMultiScanResponse
import sk.csirt.viruschecker.client.payload.ScannedFileStatus
import sk.csirt.viruschecker.hash.Md5
import sk.csirt.viruschecker.hash.Sha256
import sk.csirt.viruschecker.routing.ApiRoutes
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import sk.csirt.viruschecker.utils.await
import java.io.File
import java.io.FileInputStream
import java.time.Instant

class ScanService(
    driverUrls: List<String>,
    client: HttpClient
) : AntivirusDriverService(driverUrls, client) {
    private val logger = KotlinLogging.logger { }

    suspend fun scanFile(fileToScan: File): FileMultiScanResponse = coroutineScope {
        val deferredHashes = let {
            logger.info { "Computing MD5 and SHA-256 for $fileToScan" }
            listOf(
                async { Md5().hash(fileToScan)},
                async { Sha256().hash(fileToScan)}
            )
        }

        val driverResponses = multiDriverRequest { driverUrl, client ->
            client.post<FileScanResponse>("$driverUrl${ApiRoutes.scanFile}") {
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

        driverResponses.map {
            AntivirusScanResponse(
                status = ScannedFileStatus.valueOf(it.status.name),
                antivirus = it.antivirus,
                malwareDescription = it.malwareDescription
            )
        }.let {
            FileMultiScanResponse(
                date = Instant.now(),
                filename = driverResponses.firstOrNull()?.filename ?: "",
                status = it.maxBy { it.status }?.status
                    ?: ScannedFileStatus.NOT_AVAILABLE,
                reports = it,
                fileHashes = deferredHashes.await()
            )
        }
    }

}