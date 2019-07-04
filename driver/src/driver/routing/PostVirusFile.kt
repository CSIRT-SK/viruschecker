package driver.routing

import driver.config.Constants
import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import driver.antivirus.Antivirus
import driver.antivirus.FileScanParameters
import driver.payload.FileScanResponse
import driver.payload.toCheckResponse
import driver.antivirus.FileScanReport
import io.ktor.util.asStream
import kotlinx.io.core.Input
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

private val logger = KotlinLogging.logger { }

fun Route.checkFile(virusChecker: Antivirus) {
    post("/scanFile") {
        val multipart = call.receiveMultipart()
        logger.info("Receiving file")
        val responses = mutableListOf<FileScanResponse>()

        while (true) {
            val part = multipart.readPart() ?: break
            when (part) {
                is PartData.FileItem -> {
                    processFile(part, virusChecker).also { responses.add(it) }
                }
            }
            part.dispose()
        }

        call.respond(responses.first())
    }
}

private fun processFile(fileItem: PartData.FileItem, virusChecker: Antivirus): FileScanResponse {
    val filename = fileItem.originalFileName ?: "file${UUID.randomUUID()}"

    val report: FileScanReport = virusChecker.scanFile(
        fileItem.provider().toCheckParameters(filename, Paths.get(Constants.scanDir))
    )
    return report.toCheckResponse()
}

private fun Input.toCheckParameters(filename: String, path: Path): FileScanParameters {
    val saveFilename = "${UUID.randomUUID()}_$filename"
    val savedFile = path.resolve(saveFilename).toFile()
    driver.antivirus.logger.debug("Copying received file into ${savedFile.canonicalPath}")
    FileUtils.copyInputStreamToFile(asStream(), savedFile)
    return FileScanParameters(savedFile, filename)
}


