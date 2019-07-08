package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.asStream
import kotlinx.io.core.Input
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.gateway.routing.payload.FileScanResponse
import sk.csirt.viruschecker.gateway.service.ScanService
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

private val logger = KotlinLogging.logger { }

fun Route.scanFile(scannerService: ScanService) {
    post<MultiPartData>("/scanFile") { multipart ->
        logger.info("Receiving file")
        val responses = mutableListOf<FileScanResponse>()

        while (true) {
            val part = multipart.readPart() ?: break
            when (part) {
                is PartData.FileItem -> {
                    scannerService.scanFile(part.toTempFile()).also { responses.add(it) }
                }
            }
            part.dispose()
        }

        call.respond(responses.first())
    }
}

private fun PartData.FileItem.toTempFile(): File {
    val filename = originalFileName ?: "file${UUID.randomUUID()}"
    val tempFileName = "${UUID.randomUUID()}_$filename"
    val tempFile = Paths.get(System.getProperty("java.io.tmpdir"), tempFileName).toFile()
    FileUtils.copyInputStreamToFile(provider().asStream(), tempFile)
    return tempFile
}
