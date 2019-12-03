package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.routing.Route
import kotlinx.html.*
import sk.csirt.viruschecker.client.service.GatewayReportService
import sk.csirt.viruschecker.client.web.template.pStatus
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml
import sk.csirt.viruschecker.routing.payload.ScanStatus
import java.time.LocalDateTime
import java.time.ZoneId

@KtorExperimentalLocationsAPI
fun Route.showReport(reportService: GatewayReportService) {
    get<WebRoutes.ScanReport> { params ->
        val (sha256, md5, sha1, scanReport) = reportService.findReportBySha256(params.sha256)
        call.respondDefaultHtml {
            h2 { +"Scan report for ${scanReport.filename}" }

            +scanReport.date
                .let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }
                .let { "Time: ${it.toLocalDate()}, ${it.toLocalTime()}" }
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
            scanReport.results.forEach { report ->
                table(classes = "padding-table-columns") {
                    tr {
                        td {
                            pStatus(report.status)
                        }
                        td {
                            if(report.status == ScanStatus.INFECTED){
                                +"${report.antivirus}: ${report.malwareDescription}"
                            }else{
                                +report.antivirus
                            }
                        }
                        td {
                           +"database: ${report.virusDatabaseVersion}"
                        }
                    }
                }
                hr(classes = "lhr")
            }
        }
    }
}