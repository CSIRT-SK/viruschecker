package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import sk.csirt.viruschecker.hash.md5
import sk.csirt.viruschecker.hash.sha1
import sk.csirt.viruschecker.hash.sha256
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.*
import java.io.FileInputStream
import java.time.Instant

@ExperimentalCoroutinesApi
class DefaultDriverScanService(
    drivers: List<String>,
    client: HttpClient
) : AntivirusDriverService(drivers, client),
    FileScanService {

    private val logger = KotlinLogging.logger { }

    override suspend fun scanFile(scanParams: ScanParameters): FileHashScanResponse =
        coroutineScope {
            val (fileToScan, originalFileName) = scanParams
            logger.info { "Computing hashes for $fileToScan" }
            val sha256Deferred = async { fileToScan.sha256() }
            val sha1Deferred = async { fileToScan.sha1() }
            val md5Deferred = async { fileToScan.md5() }

            multiDriverRequest { driverUrl, client ->
                client.post<FileScanResponse>("$driverUrl${DriverRoutes.scanFile}") {
                    this.body = MultiPartFormDataContent(listOf(
                        PartData.FormItem(
                            value = scanParams.useExternalDrivers.toString(),
                            dispose = { },
                            partHeaders = Headers.Empty
                        ),
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
                                status = ScanStatusResponse.NOT_AVAILABLE,
                                virusDatabaseVersion = ""
                            )
                        )
                    )
                )
            }.flatMap {
                it.results
            }.filterNot {
                it.status == ScanStatusResponse.SCAN_REFUSED
            }.let { antivirusResponses ->
                FileHashScanResponse(
                    report = FileScanResponse(
                        date = Instant.now(),
                        filename = originalFileName,
                        results = antivirusResponses.sortedBy { it.antivirus }
                    ),
                    md5 = md5Deferred.await().value,
                    sha1 = sha1Deferred.await().value,
                    sha256 = sha256Deferred.await().value
                )
            }
        }

    override suspend fun scanFileChannel(scanParams: ScanParameters): FileHashScanChannel =
        coroutineScope {
            val (fileToScan, originalFileName) = scanParams
            logger.info { "Computing hashes for $fileToScan" }
            val sha256Deferred = async { fileToScan.sha256() }
            val sha1Deferred = async { fileToScan.sha1() }
            val md5Deferred = async { fileToScan.md5() }

            val driverChannel = multiDriverRequestChannel { driverUrl, client ->
                client.post<FileScanResponse>("$driverUrl${DriverRoutes.scanFile}") {
                    this.body = MultiPartFormDataContent(listOf(
                        PartData.FormItem(
                            value = scanParams.useExternalDrivers.toString(),
                            dispose = { },
                            partHeaders = Headers.Empty
                        ),
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
            }

            val receiveChannel = produce<AntivirusReportResponse> {
                for ((driverUrl, result) in driverChannel) {
                    result.getOrDefault(
                        FileScanResponse(
                            date = Instant.now(),
                            filename = originalFileName,
                            results = listOf(
                                AntivirusReportResponse(
                                    antivirus = "Unknown",
                                    malwareDescription = "Connection to $driverUrl was unsuccessful.",
                                    status = ScanStatusResponse.NOT_AVAILABLE,
                                    virusDatabaseVersion = ""
                                )
                            )
                        )
                    ).results.forEach {
                        send(it)
                    }
                }

            }
            FileHashScanChannel(
                md5 = md5Deferred.await().value,
                sha1 = sha1Deferred.await().value,
                sha256 = sha256Deferred.await().value,
                reportChannel = FileScanChannel(
                    date = Instant.now(),
                    filename = originalFileName,
                    results = receiveChannel
                )
            )
        }
}