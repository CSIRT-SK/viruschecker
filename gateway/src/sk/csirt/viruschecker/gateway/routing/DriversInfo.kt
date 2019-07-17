package sk.csirt.viruschecker.gateway.routing

import io.ktor.routing.Route
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.routing.service.DriverInfoService
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.UrlAntivirusDriverInfoResponse

private val logger = KotlinLogging.logger { }

@KtorExperimentalLocationsAPI
fun Route.driversInfo(checkedUrls: List<UrlAntivirusDriverInfoResponse>) {
    get<GatewayRoutes.DriversInfo> {
        logger.info { "Sending driver info to ${call.request.local.remoteHost}" }
        call.respond(checkedUrls)
    }
}


