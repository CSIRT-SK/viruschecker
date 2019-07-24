package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.locations.url
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import kotlinx.html.*
import sk.csirt.viruschecker.client.service.GatewayScanService
import sk.csirt.viruschecker.client.web.parsedArgs
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml
import sk.csirt.viruschecker.routing.payload.GatewayScanRequest
import sk.csirt.viruschecker.utils.copyToSuspend
import sk.csirt.viruschecker.utils.tempDirectory
import java.io.File
import java.util.*


@KtorExperimentalLocationsAPI
fun Route.scanFile(scanService: GatewayScanService) {

    val useExternalDrivers = "externalDrivers"

    get<WebRoutes.ScanFile> {
        call.respondDefaultHtml {
            h2 { +"Scan file" }
            +"Scan can take up to ${parsedArgs.socketTimeout.seconds} seconds."
            br(); br()
            form(
                call.url(WebRoutes.ScanFile()),
                classes = "pure-form-stacked",
                encType = FormEncType.multipartFormData,
                method = FormMethod.post
            ) {
                acceptCharset = "utf-8"

                fileInput { name = "file" }
                br()

                checkBoxInput {
                    id = useExternalDrivers
                    name = useExternalDrivers
                    value = "true"
                }
                +" Upload hash to external services like VirusTotal"
                br(); br()
//                label {
//                    htmlFor = useExternalDrivers
//                    +"Would you like to use external services like VirusTotal as well?"
//                    br()
//                    listOf(
//                        false.toString() to "Do not use external services (default)",
//                        true.toString() to "Upload hash to external services"
//                    ).forEach { (virusTotalOption, label) ->
//                        input {
//                            type = InputType.checkBox
//                            name = useExternalDrivers
//                            value = virusTotalOption
//
//                            label {
//                                htmlFor = virusTotalOption
//                                +label
//                            }
//                        }
//                    }

//                    listOf(
//                        false.toString() to "Do not use external services (default)",
//                        true.toString() to "Upload hash to external services"
//                    ).forEach { (virusTotalOption, label) ->
//                        input {
//                            type = InputType.radio
//                            name = useExternalDrivers
//                            value = virusTotalOption
//                            label {
//                                htmlFor = virusTotalOption
//                                +label
//                            }
//                        }
//                    }
//            }
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
        var multiScanRequest = GatewayScanRequest(
            fileToScan = File(""),
            originalFilename = "",
            useExternalDrivers = false
        )

        val fileId = UUID.randomUUID().toString()

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    multiScanRequest = multiScanRequest.copy(
                        useExternalDrivers = part.value.toBoolean()
                    )
                }
                is PartData.FileItem -> {
                    val file = File(
                        tempDirectory,
                        "$fileId-${part.originalFileName}"
                    )

                    part.streamProvider()
                        .use { its -> file.outputStream().buffered().use { its.copyToSuspend(it) } }
                    multiScanRequest = multiScanRequest.copy(
                        fileToScan = file,
                        originalFilename = part.originalFileName ?: file.name
                    )
                }
            }
            part.dispose()
        }

        val responseLambda: suspend (ApplicationCall) -> Unit =
            if (multiScanRequest.fileToScan.exists().not()) {
                { call ->
                    call.respond(HttpStatusCode.InternalServerError, "File was not uploaded")
                }
            } else {
                val report = scanService.scanFile(multiScanRequest);
                { call ->
                    call.respondRedirect(call.url(WebRoutes.ScanReport(report.sha256)), false)
                }
            }
        responseLambda(call)
    }
}