package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.asStream
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.gateway.routing.service.DriverScanService
import sk.csirt.viruschecker.gateway.routing.service.ScanParameters
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.FileMultiScanResponse
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger { }

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.multiScanFile(scanService: DriverScanService) {
    post<GatewayRoutes.MultiScanFile> {
        val multipart = call.receiveMultipart()
        logger.info("Receiving file")
        var response: FileMultiScanResponse? = null

        while (true) {
            val part = multipart.readPart() ?: break
            when (part) {
                is PartData.FileItem -> {
                    response = scanService.scanFile(
                        ScanParameters(
                            fileToScan = part.toTempFile(),
                            originalFilename = part.originalFileName ?: "scan${Instant.now()}"
                        )
                    )
                }
            }
            part.dispose()
        }

        if (response == null) {
            call.respond(HttpStatusCode.BadRequest, "File was not uploaded.")
        } else {
            call.respond(response)
        }
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

