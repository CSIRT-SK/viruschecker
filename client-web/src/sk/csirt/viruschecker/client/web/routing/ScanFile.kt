package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.locations
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import kotlinx.html.*
import sk.csirt.viruschecker.client.service.GatewayScanService
import sk.csirt.viruschecker.client.service.ScanParameters
import sk.csirt.viruschecker.client.web.parsedArgs
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml
import sk.csirt.viruschecker.routing.payload.ScanStatus
import sk.csirt.viruschecker.utils.copyToSuspend
import java.io.File


@KtorExperimentalLocationsAPI
fun Route.scanFile(scanService: GatewayScanService) {

    val useExternalDrivers = "externalDrivers"

    get<WebRoutes.ScanFile> {
        call.respondDefaultHtml {
            h2 { +"Scan file" }
            +"Scan can take up to ${parsedArgs.socketTimeout.seconds} seconds."
            br(); br()
            form(
//                call.url(WebRoutes.ScanFile()),
                locations.href(WebRoutes.ScanFile()),
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

                submitInput(classes = "pure-button pure-button-primary") { value = "Scan file" }
            }
        }
    }


    post<WebRoutes.ScanFile> {
        val multipart = call.receiveMultipart()
        var scanParameters = ScanParameters(
            fileToScan = File(""),
            originalFilename = "",
            useExternalDrivers = false
        )

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    scanParameters = scanParameters.copy(
                        useExternalDrivers = part.value.toBoolean()
                    )
                }
                is PartData.FileItem -> {
                    val file = createTempFile()
                    part.streamProvider()
                        .use { inputStream ->
                            file.outputStream()
                                .buffered()
                                .use {
                                    inputStream.copyToSuspend(it)
                                }
                        }
                    scanParameters = scanParameters.copy(
                        fileToScan = file,
                        originalFilename = part.originalFileName ?: file.name
                    )
                }
            }
            part.dispose()
        }

        if (scanParameters.fileToScan.exists().not()) {
            call.respond(HttpStatusCode.InternalServerError, "File was not uploaded")
        } else {
            val scanResult = scanService.scanFile(scanParameters)
                .let { scanResult ->
                    scanResult.copy(
                        report = scanResult.report.copy(
                            results = scanResult.report.results.filterNot {
                                it.status == ScanStatus.SCAN_REFUSED
                            }
                        )
                    )
                }
            call.respondRedirect(locations.href(WebRoutes.ScanReport(scanResult.sha256)), false)
        }
    }
}