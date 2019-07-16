package sk.csirt.viruschecker.driver.routing

import sk.csirt.viruschecker.driver.config.Constants
import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import sk.csirt.viruschecker.driver.antivirus.Antivirus
import sk.csirt.viruschecker.driver.antivirus.FileScanParameters
import sk.csirt.viruschecker.driver.antivirus.FileScanReport
import io.ktor.util.asStream
import kotlinx.io.core.Input
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

private val logger = KotlinLogging.logger { }

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.scanFile(antivirus: Antivirus) {
    post<DriverRoutes.ScanFile> {
        val multipart = call.receiveMultipart()
        logger.info("Receiving file")
        val responses = mutableListOf<FileScanResponse>()

        while (true) {
            val part = multipart.readPart() ?: break
            when (part) {
                is PartData.FileItem -> {
                    processFile(part, antivirus).also { responses.add(it) }
                }
            }
            part.dispose()
        }

        call.respond(responses.first())
    }
}

@KtorExperimentalAPI
private suspend fun processFile(fileItem: PartData.FileItem, virusChecker: Antivirus): FileScanResponse {
    val filename = fileItem.originalFileName ?: "file${UUID.randomUUID()}"

    val report: FileScanReport = virusChecker.scanFile(
        fileItem.provider().toCheckParameters(filename, Paths.get(Constants.scanDir))
    )
    return report.toFileScanResponse()
}

@KtorExperimentalAPI
private fun Input.toCheckParameters(filename: String, path: Path): FileScanParameters {
    val saveFilename = "${UUID.randomUUID()}_$filename"
    val savedFile = path.resolve(saveFilename).toFile()
    sk.csirt.viruschecker.driver.antivirus.logger.debug("Copying received file into ${savedFile.canonicalPath}")
    FileUtils.copyInputStreamToFile(asStream(), savedFile)
    return FileScanParameters(savedFile, filename)
}

fun FileScanReport.toFileScanResponse() = FileScanResponse(
    filename = filename,
    antivirus = antivirus.commonName,
    status = FileScanResponse.Status.valueOf(status.name),
    malwareDescription = malwareDescription
)

