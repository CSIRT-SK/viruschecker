package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.routing.Route
import kotlinx.html.*
import sk.csirt.viruschecker.client.service.GatewayReportService
import sk.csirt.viruschecker.client.web.template.pStatus
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml
import sk.csirt.viruschecker.routing.payload.ScanStatusResponse
import java.time.LocalDateTime
import java.time.ZoneId

@KtorExperimentalLocationsAPI
fun Route.showAllReports(reportService: GatewayReportService) {
    get<WebRoutes.AllScanReports> {
        val scanReports = reportService.findAllReports()
        call.respondDefaultHtml {
            h2 { +"All scan reports" }
            scanReports.forEach { (sha256, md5, sha1, scanReport) ->
                br()
                strong { +"File ${scanReport.filename}" }
                br()
                +scanReport.date
                    .let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }
                    .let { "Scan time: ${it.toLocalDate()}, ${it.toLocalTime()}" }

                pStatus(scanReport.status)
                p {
                    listOf(
                        "SHA-256" to sha256,
                        "SHA-1" to sha1,
                        "MD5" to md5
                    ).forEach { (algorithm, value) ->
                        +"$algorithm: $value"
                        br()
                    }
                }
                p {
                    +"Antivirus reports:"
                    br()
                    scanReport.results.forEach { report ->
                        val status = if(report.status >= ScanStatusResponse.NOT_AVAILABLE)
                            report.malwareDescription
                        else
                            report.status.toString()
                        +"${report.antivirus}:${report.virusDatabaseVersion}; $status"
                        br()
                    }
                }
                hr(classes = "lhr")
            }
        }
    }
}
