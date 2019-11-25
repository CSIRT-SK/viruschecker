package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.routing.Route
import kotlinx.html.*
import sk.csirt.viruschecker.client.service.GatewayReportService
import sk.csirt.viruschecker.client.web.template.respondMultipleReportsHtml

@KtorExperimentalLocationsAPI
fun Route.showReportsBy(reportService: GatewayReportService) {
    get<WebRoutes.ScanReportsBy> { params ->
        val scanReports = if(params.search.isBlank())
            emptyList()
        else
            reportService.findReportsBy(params.search)

        call.respondMultipleReportsHtml(scanReports){
            h2 { +"Scan reports" }
            form(
                WebRoutes.scanReportsBy,
                classes = "pure-form-stacked",
                encType = FormEncType.multipartFormData,
                method = FormMethod.get
            ) {
                acceptCharset = "utf-8"

                textInput {
                    name = "search"
                    value = params.search
                }
                br()
                submitInput(classes = "pure-button pure-button-primary") { value = "Search" }
                br(); br(); br()
            }
        }
    }
}