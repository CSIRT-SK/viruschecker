package sk.csirt.viruschecker.driver.routing

import sk.csirt.viruschecker.driver.config.Constants
import io.ktor.application.call
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import sk.csirt.viruschecker.driver.antivirus.Antivirus
import sk.csirt.viruschecker.driver.antivirus.FileScanParameters
import sk.csirt.viruschecker.driver.routing.payload.FileScanResponse
import sk.csirt.viruschecker.driver.routing.payload.toCheckResponse
import sk.csirt.viruschecker.driver.antivirus.FileScanReport
import io.ktor.util.asStream
import kotlinx.io.core.Input
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

private val logger = KotlinLogging.logger { }

fun Route.scanFile(virusChecker: Antivirus) {
    post<MultiPartData>("/scanFile") { multipart ->
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
    sk.csirt.viruschecker.driver.antivirus.logger.debug("Copying received file into ${savedFile.canonicalPath}")
    FileUtils.copyInputStreamToFile(asStream(), savedFile)
    return FileScanParameters(savedFile, filename)
}


