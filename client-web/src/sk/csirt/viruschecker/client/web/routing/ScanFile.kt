package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.locations.url
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import kotlinx.coroutines.*
import kotlinx.html.*
import sk.csirt.viruschecker.client.service.ScanService
import sk.csirt.viruschecker.client.web.service.ScanReportService
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml
import sk.csirt.viruschecker.hash.HashAlgorithm
import sk.csirt.viruschecker.utils.copyToSuspend
import sk.csirt.viruschecker.utils.tempDirectory
import java.io.File
import java.util.*

@KtorExperimentalLocationsAPI
fun Route.scanFile(scanReportService: ScanReportService, scanService: ScanService) {
    get<WebRoutes.ScanFile> {
        call.respondDefaultHtml {
            h2 { +"Scan file" }

            form(
                call.url(WebRoutes.ScanFile()),
                classes = "pure-form-stacked",
                encType = FormEncType.multipartFormData,
                method = FormMethod.post
            ) {
                acceptCharset = "utf-8"

                fileInput { name = "file" }
                br()

                submitInput(classes = "pure-button pure-button-primary") { value = "Scan file" }
            }
        }
    }


    /**
     * Registers a POST route for [Upload] that actually read the bits sent from the client and creates a new video
     * using the [database] and the [uploadDir].
     */
    post<WebRoutes.ScanFile> {
        val multipart = call.receiveMultipart()
//        var title = ""
        var fileToScan: File? = null
        val fileId = UUID.randomUUID().toString()

        while (true) {
            val part = multipart.readPart() ?: break
            if (part is PartData.FileItem) {
                val file = File(
                    tempDirectory,
                    "$fileId-${part.originalFileName}"
                )

                part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyToSuspend(it) } }
                fileToScan = file
            }
            part.dispose()
        }

        val responseLambda: suspend (ApplicationCall) -> Unit =
            if (fileToScan == null) {
                { call ->
                    call.respond(HttpStatusCode.InternalServerError, "File was not uploaded")
                }
            } else {

                val report = scanService.scanFile(fileToScan)
                scanReportService.save(report, fileId);
                { call ->
                    call.respondRedirect(call.url(WebRoutes.ScanReport(fileId)), false)
                }
            }
        responseLambda(call)
    }
}