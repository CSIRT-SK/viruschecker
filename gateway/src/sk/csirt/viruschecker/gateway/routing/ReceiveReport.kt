package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
import sk.csirt.viruschecker.routing.GatewayRoutes

private val logger = KotlinLogging.logger { }

@KtorExperimentalLocationsAPI
fun Route.findByHash(
    scanReportService: PersistentScanReportService
) {
    get<GatewayRoutes.ScanReport> { params ->
        val hash = params.sha256
        logger.info { "Requested report for sha256 $hash." }
        val scanReport = scanReportService.findBySha256(hash)

        if (scanReport == null) {
            call.respond(HttpStatusCode.NoContent, "Hash not found!")
        } else {
            call.respond(scanReport)
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.findAll(
    scanReportService: PersistentScanReportService
) {
    get<GatewayRoutes.AllScanReports> {
        logger.info { "Requested all reports." }
        val scanReports = scanReportService.findAll()
        call.respond(scanReports)
    }
}


@KtorExperimentalLocationsAPI
fun Route.findBy(
    scanReportService: PersistentScanReportService
) {
    get<GatewayRoutes.ScanReportBy> { params ->
        val searchWords = params.searchWordsList
        logger.info { "Requested reports for search words: $searchWords." }
        val scanReports = scanReportService.findBy(searchWords)
        call.respond(scanReports)
    }
}

