package sk.csirt.viruschecker.gateway.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.payload.AntivirusDriverResponse
import sk.csirt.viruschecker.gateway.payload.AntivirusResponse
import sk.csirt.viruschecker.gateway.payload.FileScanResponse
import sk.csirt.viruschecker.gateway.payload.ScannedFileStatus
import java.io.File
import java.io.FileInputStream
import java.time.Instant

class ScannerService(
    private val driverUrls: List<String>,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    fun scanPartData(part: PartData.FileItem): FileScanResponse = runBlocking {
        val driverResponses = driverUrls.map { url ->
            async {
                client.post<AntivirusDriverResponse>("$url/scanFile") {
                    this.body = MultiPartFormDataContent(listOf(part))
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
                antivirus = it.antivirus
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

    //    fun scanStream(inputStream: InputStream)
    fun test() {
//        val fileToScan = File("gradle.properties")
        val fileToScan = File("go.zip")
//    val fileToScan = File("eicar.exe")
//     val fileToScan = File("goWEicar.zip")

        //    val fileToScan = parsedArgs.fileToScan
        val fileScanReport = runBlocking {
            driverUrls.map { url ->
                async {
                    client.post<AntivirusDriverResponse>("$url/scanFile") {
                        this.body = MultiPartFormDataContent(
                            listOf(
                                PartData.FileItem(
                                    partHeaders = Headers.build {
                                        this[HttpHeaders.ContentDisposition] =
                                            ContentDisposition.File.withParameter(
                                                "filename",
                                                fileToScan.name
                                            )
                                                .toString()
                                    },
                                    dispose = { },
                                    provider = { FileInputStream(fileToScan).asInput() }
                                )
                            )
                        )
                    }.also {
                        logger.info("Retrieved report from $url: $it")
                    }
                }
            }.map {
                it.await()
            }.map {
                AntivirusResponse(
                    status = ScannedFileStatus.valueOf(it.status.name),
                    antivirus = it.antivirus
                )
            }.let {
                FileScanResponse(
                    filename = fileToScan.name,
                    reports = it,
                    date = Instant.now(),
                    status = it.maxBy { it.status }?.status
                        ?: ScannedFileStatus.NOT_AVAILABLE
                )
            }
        }

        logger.info("Received report: $fileScanReport")
    }

}