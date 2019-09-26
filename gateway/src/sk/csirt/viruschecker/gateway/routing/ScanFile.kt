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
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.routing.service.FileScanService
import sk.csirt.viruschecker.gateway.routing.service.ScanParameters
import sk.csirt.viruschecker.gateway.routing.utils.toTempFile
import sk.csirt.viruschecker.routing.GatewayRoutes
import java.io.File
import java.time.Instant

private val logger = KotlinLogging.logger { }

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.multiScanFile(scanService: FileScanService) {
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
}

