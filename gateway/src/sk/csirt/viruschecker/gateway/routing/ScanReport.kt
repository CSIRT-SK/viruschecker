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
        logger.info { "Retrieving report for sha256 $hash." }
        val scanReport = scanReportService.findBySha256(hash)

        if (scanReport == null) {
            call.respond(HttpStatusCode.NoContent, "Hash not found!")
        } else {
            call.respond(scanReport)
        }
    }
}