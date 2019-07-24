package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.routing.Route
import kotlinx.html.*
import sk.csirt.viruschecker.client.service.ReportByHashService
import sk.csirt.viruschecker.client.web.template.pAlert
import sk.csirt.viruschecker.client.web.template.pOk
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml
import sk.csirt.viruschecker.routing.payload.ScanStatusResponse
import java.time.LocalDateTime
import java.time.ZoneId

@KtorExperimentalLocationsAPI
fun Route.showReport(reportService: ReportByHashService) {
    get<WebRoutes.ScanReport> { params ->
        val (sha256, md5, sha1, scanReport) =
            reportService.findReportBySha256(params.sha256)
        call.respondDefaultHtml {
            h2 { +"Scan report for ${scanReport.filename}" }

            +scanReport.date
                .let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }
                .let { "Time: ${it.toLocalDate()}, ${it.toLocalTime()}" }
//            br()
//
            p {
                strong { +"Scan result: " }
                pStatus(scanReport.status)
            }
            br()

            listOf(
                "SHA-256" to sha256,
                "SHA-1" to sha1,
                "MD5" to md5
            ).forEach { (algorithm, value) ->
                p {
                    +algorithm
                    br()
                    +value
                }
            }

            br(); br()
            strong { +"Antivirus reports" }
            hr()
            scanReport.results.forEach {
                p {
                    +"Antivirus: ${it.antivirus}"
                    br()
                    +"Description: ${it.malwareDescription}"
                    pStatus(it.status)
                }
                hr(classes = "lhr")
            }
        }
    }

}

@HtmlTagMarker
private fun FlowContent.pStatus(status: ScanStatusResponse) {
    when (status) {
        ScanStatusResponse.OK -> pOk {
            +"OK"
        }
        ScanStatusResponse.INFECTED -> pAlert {
            +"INFECTED"
        }
        else -> p {
            +"NOT AVAILABLE"
        }
    }
}