package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.payload.FileScanResponse
import sk.csirt.viruschecker.gateway.service.ScannerService

private val logger = KotlinLogging.logger { }

fun Route.scanFile(scannerService: ScannerService) {
    post("/scanFile") {
        val multipart = call.receiveMultipart()
        logger.info("Receiving file")
        val responses = mutableListOf<FileScanResponse>()

        while (true) {
            val part = multipart.readPart() ?: break
            when (part) {
                is PartData.FileItem -> {
                    scannerService.scanPartData(part).also { responses.add(it) }
                }
            }
            part.dispose()
        }

        call.respond(responses.first())
    }
}
