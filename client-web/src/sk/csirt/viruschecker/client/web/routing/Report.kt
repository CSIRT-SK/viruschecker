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
import sk.csirt.viruschecker.routing.payload.ScannedFileStatus
import java.time.LocalDateTime
import java.time.ZoneId

@KtorExperimentalLocationsAPI
fun Route.showReport(reportService: ReportByHashService) {
    get<WebRoutes.ScanReport> { params ->
        val scanReport = reportService.findReportBySha256(params.hash)
        call.respondDefaultHtml {
            h2 { +"Scan report for ${scanReport.filename}" }

            +"Date: ${scanReport.date.let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }}"
//            br()
//
            p {
                strong { +"Scan result: " }
                pStatus(scanReport.status)
            }

            p {
                +"SHA-256"
                br()
                +scanReport.sha256
            }
            scanReport.otherHashes.forEach {
                p {
                    +it.algorithm
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
                    pStatus(it.status)
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