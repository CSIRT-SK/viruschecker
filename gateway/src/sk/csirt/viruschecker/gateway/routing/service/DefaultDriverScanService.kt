package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.config.Drivers
import sk.csirt.viruschecker.hash.*
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.*
import java.io.FileInputStream
import java.time.Instant

class DefaultDriverScanService(
    drivers: Drivers,
    client: HttpClient
) : AntivirusDriverService(drivers, client),
    FileScanService {
    private val logger = KotlinLogging.logger { }

    override suspend fun scanFile(scanParams: GatewayScanRequest): FileHashScanResponse =
        coroutineScope {
            val (fileToScan, originalFileName) = scanParams
            logger.info { "Computing hashes for $fileToScan" }
            val sha256Deferred = async(Dispatchers.IO) { fileToScan.sha256() }
            val sha1Deffered = async(Dispatchers.IO) { fileToScan.sha1() }
            val md5Deffered = async(Dispatchers.IO) { fileToScan.md5() }

            multiDriverRequest(
                useExternalDrivers = scanParams.useExternalDrivers
            ) { driverUrl, client ->
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
                        date = Instant.now(),
                        filename = originalFileName,
                        results = listOf(
                            AntivirusReportResponse(
                                antivirus = "Unknown",
                                malwareDescription = "Connection to $driverUrl was unsuccessful.",
                                status = ScanStatusResponse.NOT_AVAILABLE
                            )
                        )
                    )
                )
            }.flatMap {
                it.results
            }.let {
                FileHashScanResponse(
                    report = FileScanResponse(
                        date = Instant.now(),
                        filename = originalFileName,
                        results = it.sortedBy { it.antivirus }
                    ),
                    md5 = md5Deffered.await().value,
                    sha1 = sha1Deffered.await().value,
                    sha256 = sha256Deferred.await().value
                )
            }
        }

}