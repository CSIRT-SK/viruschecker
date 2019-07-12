package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.cache.service.ScanReportService
import sk.csirt.viruschecker.routing.GatewayRoutes

private val logger = KotlinLogging.logger { }

@KtorExperimentalLocationsAPI
fun Route.findByHash(scanReportService: ScanReportService){
    get<GatewayRoutes.ScanReport>{ params ->
        logger.info { "Retrieving report for hash ${params.sha256}" }
        val scanReport = scanReportService.findBySha256(params.sha256)
        if(scanReport == null){
            call.respond(HttpStatusCode.NotFound, "Hash not found!")
        }else{
            call.respond(scanReport)
        }
    }
}