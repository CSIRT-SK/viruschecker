package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.asStream
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.gateway.routing.service.FileScanService
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.GatewayScanRequest
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger { }

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.multiScanFile(scanService: FileScanService) {
    post<GatewayRoutes.MultiScanFile> {
        val multipart = call.receiveMultipart()
        logger.info("Receiving file")

        var fileToScan: File? = null
        var originalFilename = ""
        var useExternalDrivers = false

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    useExternalDrivers = part.value.toBoolean()
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
                    "${if (useExternalDrivers) "also" else "no"} external drivers."
        }

        if (fileToScan == null) {
            call.respond(HttpStatusCode.BadRequest, "File was not received.")
        }

        runCatching {
            scanService.scanFile(
                GatewayScanRequest(
                    fileToScan = fileToScan!!,
                    useExternalDrivers = useExternalDrivers,
                    originalFilename = originalFilename
                )
            )
        }.onSuccess {
            call.respond(it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                "File was not scanned. Reason is ${it.message}"
            )
        }.getOrThrow()
    }
}

@KtorExperimentalAPI
private fun PartData.FileItem.toTempFile(): File {
    val filename = originalFileName ?: "file${UUID.randomUUID()}"
    val tempFileName = "${UUID.randomUUID()}_$filename"
    val tempFile = Paths.get(System.getProperty("java.io.tmpdir"), tempFileName).toFile()
    FileUtils.copyInputStreamToFile(provider().asStream(), tempFile)
    return tempFile
}

