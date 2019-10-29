package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.readText
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
import sk.csirt.viruschecker.utils.JsonConverter
import java.io.File
import java.time.Instant

private val logger = KotlinLogging.logger { }

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.multiScanFile(scanService: FileScanService, jsonConverter: JsonConverter) {
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

        val useExternalServices = (incoming.receiveOrNull() as? Frame.Text)
            ?.readText()?.also { logger.debug { "Received WebSocket message: '$it'" } }
            ?.takeIf { "useExternalDrivers: true" == it }
            ?.let { true } ?: false.also { logger.debug { "Received WebSocket message: '$it'" } }

        val originalFilename = (incoming.receiveOrNull() as? Frame.Text)
            ?.readText()?.also { logger.debug { "Received WebSocket message: '$it'" } }
            ?: "".also { logger.debug { "Received WebSocket message: '$it'" } }

        val fileToScan: File = (incoming.receive() as Frame.Binary)
            .readBytes()
            .toTempFile()

//        for (frame in incoming) {
//            when (frame) {
//                is Frame.Text -> {
//                    val text = frame.readText()
//                    if ("useExternalServices" == text) {
//                        useExternalServices = true
//                    } else {
//                        originalFilename = text
//                    }
//                }
//                is Frame.Binary -> {
//                    fileToScan = frame.readBytes().toTempFile(originalFilename)
//                }
//            }
//        }
        logger.info {
            "Received WebSocket request to scan file $originalFilename using " +
                    "${if (useExternalServices) "also" else "no"} external drivers."
        }

//        if (fileToScan == null) {
//            outgoing.close()
//            send(
//                Frame.Close(
//                    CloseReason(
//                        CloseReason.Codes.CANNOT_ACCEPT,
//                        "Binary file not received"
//                    )
//                )
//            )
//        }
        val scanChannel = scanService.run {
            scanFileChannel(
                ScanParameters(
                    fileToScan = fileToScan,
                    useExternalDrivers = useExternalServices,
                    originalFilename = originalFilename
                )
            )
        }

//        send(Frame.Text(scanChannel.md5))
//        send(Frame.Text(scanChannel.sha1))
//        send(Frame.Text(scanChannel.sha256))

        for (antivirusReport in scanChannel) {
            val message = Frame.Text(jsonConverter.toJson(antivirusReport))
            logger.debug { "Sending via WebSocket: $message " }
            send(message)
        }
        close()
    }
}

