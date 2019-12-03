package sk.csirt.viruschecker.driver.routing

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.asStream
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.io.core.Input
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.antivirus.Antivirus
import sk.csirt.viruschecker.driver.antivirus.AntivirusReportResult
import sk.csirt.viruschecker.driver.antivirus.FileScanParameters
import sk.csirt.viruschecker.driver.antivirus.FileScanResult
import sk.csirt.viruschecker.driver.config.Constants
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import sk.csirt.viruschecker.routing.payload.ScanFileWebSocketParameters
import sk.csirt.viruschecker.routing.payload.ScanStatus
import sk.csirt.viruschecker.utils.fromJson
import sk.csirt.viruschecker.utils.json
import sk.csirt.viruschecker.utils.toTempFile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger { }

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.scanFile(
    antivirus: Antivirus
) {
    post<DriverRoutes.ScanFile> {
        val multipart = call.receiveMultipart()
        logger.info("Receiving file")
        var useExternalServices = false
        var fileItem: PartData.FileItem? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    useExternalServices = part.value.toBoolean()
                }
                is PartData.FileItem -> {
                    fileItem = part
                }
            }
        }

        if (fileItem == null) {
            call.respond(HttpStatusCode.BadRequest, "File was not received.")
        }

        fileItem?.let { fileItem ->
            val response = processFile(fileItem, useExternalServices, antivirus)
            fileItem.dispose()

            call.respond(response)
        }
    }

    webSocket(DriverRoutes.scanFileWebSocket) {
        logger.info { "WebSocket connection established" }

        val (useExternalServices, originalFilename) = (incoming.receiveOrNull() as? Frame.Text)
            ?.readText()?.also { logger.debug { "Received WebSocket message: '$it'" } }
            ?.fromJson<ScanFileWebSocketParameters>() ?: ScanFileWebSocketParameters(false, "")

        val fileToScan: File = (incoming.receive() as Frame.Binary)
            .readBytes()
            .toTempFile()

        val scanChannel = antivirus.run {
            scanFileChannel(
                FileScanParameters(
                    fileToScan = fileToScan,
                    externalServicesAllowed = useExternalServices,
                    originalFileName = originalFilename
                )
            )
        }

        for (scanResult in scanChannel) {
            logger.debug { "Sending via WebSocket: $scanResult" }
            send(scanResult.toAntivirusReportResponse().json())
        }
        logger.debug { "Closing WebSocket connection." }
        close(CloseReason(CloseReason.Codes.NORMAL, "WebSocket connection finished normally"))
    }
}

@KtorExperimentalAPI
private suspend fun processFile(
    fileItem: PartData.FileItem,
    useExternalServices: Boolean,
    virusChecker: Antivirus
): FileScanResponse {
    val filename = fileItem.originalFileName?.replace(" ", "-")
        ?: "file${UUID.randomUUID()}"

    val report: FileScanResult = virusChecker.scanFileAndClean(
        fileItem.provider().toCheckParameters(
            filename,
            Paths.get(Constants.scanDir),
            useExternalServices
        )
    )
    return report.toFileScanResponse()
}

@KtorExperimentalAPI
private fun Input.toCheckParameters(
    filename: String,
    path: Path,
    useExternalServices: Boolean
): FileScanParameters {
    val saveFilename = "${UUID.randomUUID()}_$filename"
    val savedFile = path.resolve(saveFilename).toFile()
    sk.csirt.viruschecker.driver.antivirus.logger.debug("Copying received file into ${savedFile.canonicalPath}")
    FileUtils.copyInputStreamToFile(asStream(), savedFile)
    return FileScanParameters(savedFile, filename, useExternalServices)
}

fun AntivirusReportResult.toAntivirusReportResponse() = AntivirusReportResponse(
    antivirus = antivirusName,
    status = ScanStatus.valueOf(status.name),
    malwareDescription = malwareDescription,
    virusDatabaseVersion = virusDatabaseVersion
)

fun FileScanResult.toFileScanResponse() = FileScanResponse(
    date = Instant.now(),
    filename = filename,
    results = scanReport.reports.map { it.toAntivirusReportResponse() }
)