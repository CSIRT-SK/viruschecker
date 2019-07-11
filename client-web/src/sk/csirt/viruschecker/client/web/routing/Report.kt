package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Route
import io.ktor.locations.get
import io.ktor.response.respond
import kotlinx.html.*
import sk.csirt.viruschecker.client.payload.ScannedFileStatus
import sk.csirt.viruschecker.client.web.service.ScanReportService
import sk.csirt.viruschecker.client.web.template.pAlert
import sk.csirt.viruschecker.client.web.template.pOk
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml
import java.time.LocalDateTime
import java.time.ZoneId

@KtorExperimentalLocationsAPI
fun Route.showReport(reportService: ScanReportService) {
    get<WebRoutes.ScanReport> { params ->
        val scanReport = reportService.findById(params.id) ?: run {
            call.respond(HttpStatusCode.BadRequest, "Id is incorrect.")
            return@get
        }
        call.respondDefaultHtml {
            h2 { +"Scan report for ${scanReport.filename}" }

            +"Date: ${scanReport.date.let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }}"
//            br()
//
            p {
                strong { +"Scan result: " }
                pStatus(scanReport.status)
            }

            scanReport.fileHashes.forEach {
                p{
                    +it.algorithm.toString()
                    br()
                    +it.value
                }
            }

            br(); br()
            h3 { +"Antivirus reports" }
            hr()
            scanReport.reports.forEach {
                p {
                    +"Antivirus: ${it.antivirus}"
                    br()
                    +"Description: ${it.malwareDescription}"
                    pStatus(scanReport.status)
                }
                hr(classes = "lhr")
            }

        }
    }

}

@HtmlTagMarker
private fun FlowContent.pStatus(status: ScannedFileStatus) {
    when (status) {
        ScannedFileStatus.OK -> pOk {
            +"OK"
        }
        ScannedFileStatus.INFECTED -> pAlert {
            +"INFECTED"
        }
        else -> p {
            +"NOT AVAILABLE"
        }
    }
}