package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
import sk.csirt.viruschecker.routing.GatewayRoutes

private val logger = KotlinLogging.logger { }

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