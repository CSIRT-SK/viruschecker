package sk.csirt.viruschecker.gateway.routing

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
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.receiveOrNull
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.routing.service.FileScanService
import sk.csirt.viruschecker.gateway.routing.service.ScanParameters
import sk.csirt.viruschecker.gateway.routing.utils.toTempFile
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.HashResponse
import sk.csirt.viruschecker.routing.payload.ScanFileWebSocketParameters
import sk.csirt.viruschecker.utils.fromJson
import sk.csirt.viruschecker.utils.json
import sk.csirt.viruschecker.utils.toTempFile
import java.io.File
import java.time.Instant

private val logger = KotlinLogging.logger { }

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.multiScanFile(
    scanService: FileScanService
) {
    post<GatewayRoutes.MultiScanFile> {
        val multipart = call.receiveMultipart()
        logger.info("Receiving file")

        var fileToScan: File? = null
        var originalFilename = ""
        var useExternalServices = false

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    useExternalServices = part.value.toBoolean()
                }
                is PartData.FileItem -> {
                    fileToScan = part.toTempFile()
                    originalFilename = part.originalFileName ?: "scan${Instant.now()}"
                }
            }
            part.dispose()
        }

        logger.info {
            "Received request to scan file $originalFilename using " +
                    "${if (useExternalServices) "also" else "no"} external drivers."
        }

        if (fileToScan == null) {
            call.respond(HttpStatusCode.BadRequest, "File was not received.")
        }

        scanService.scanFile(
            ScanParameters(
                fileToScan = fileToScan!!,
                useExternalDrivers = useExternalServices,
                originalFilename = originalFilename
            )
        ).also {
            call.respond(it)
        }
    }

    webSocket(GatewayRoutes.multiScanFileWebSocket) {
        logger.info { "WebSocket connection established" }

        val (useExternalServices, originalFilename) = (incoming.receiveOrNull() as? Frame.Text)
            ?.readText()?.also { logger.debug { "Received WebSocket message: '$it'" } }
            ?.fromJson<ScanFileWebSocketParameters>() ?: ScanFileWebSocketParameters(false, "")

        val fileToScan: File = (incoming.receive() as Frame.Binary)
            .readBytes()
            .toTempFile()

        logger.info {
            "Received WebSocket request to scan file $originalFilename using " +
                    "${if (useExternalServices) "also" else "no"} external drivers."
        }

        val scanChannel = scanService.run {
            scanFileChannel(
                ScanParameters(
                    fileToScan = fileToScan,
                    useExternalDrivers = useExternalServices,
                    originalFilename = originalFilename
                )
            )
        }

        send(
            HashResponse(
                md5 = scanChannel.md5,
                sha1 = scanChannel.sha1,
                sha256 = scanChannel.sha256
            ).json()
        )

        for (antivirusReport in scanChannel) {
            val message = antivirusReport.json()
            logger.debug { "Sending via WebSocket: $message " }
            send(message)
        }
        close(CloseReason(CloseReason.Codes.NORMAL, "WebSocket connection finished normally"))
    }
}

