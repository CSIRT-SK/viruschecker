package sk.csirt.viruschecker.gateway.routing.service

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
import sk.csirt.viruschecker.hash.*
import sk.csirt.viruschecker.routing.payload.AntivirusScanResponse
import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import sk.csirt.viruschecker.routing.payload.ScannedFileStatus
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import sk.csirt.viruschecker.utils.await
import java.io.FileInputStream
import java.time.Instant

class DefaultDriverScanService(
    driverUrls: List<String>,
    client: HttpClient
) : AntivirusDriverService(driverUrls, client),
    DriverScanService {
    private val logger = KotlinLogging.logger { }

    override suspend fun scanFile(scanParams: ScanParameters): FileMultiScanResponse =
        coroutineScope {
            val (fileToScan, originalFileName) = scanParams
            val sha256Deferred = async { fileToScan.sha256() }
            val otherHashesDeferred = let {
                logger.info { "Computing MD5 and SHA-256 for $fileToScan" }
                listOf(
                    async { fileToScan.md5() }
                )
            }

            val driverResponses = multiDriverRequest { driverUrl, client ->
                client.post<FileScanResponse>("$driverUrl${DriverRoutes.scanFile}") {
                    this.body = MultiPartFormDataContent(listOf(
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
                        filename = originalFileName,
                        malwareDescription = "Connection to $driverUrl was unsuccessful.",
                        status = FileScanResponse.Status.NOT_AVAILABLE,
                        antivirus = "Unknown"
                    )
                )
            }
            driverResponses
                .map {
                    AntivirusScanResponse(
                        status = ScannedFileStatus.valueOf(it.status.name),
                        antivirus = it.antivirus,
                        malwareDescription = it.malwareDescription
                    )
                }.let {
                    FileMultiScanResponse(
                        date = Instant.now(),
                        filename = originalFileName,
                        status = it.maxBy { it.status }?.status
                            ?: ScannedFileStatus.NOT_AVAILABLE,
                        reports = it,
                        sha256 = sha256Deferred.await().value,
                        otherHashes = otherHashesDeferred.await()
                    )
                }
        }

}