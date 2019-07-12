package sk.csirt.viruschecker.gateway.routing

import io.ktor.routing.Route
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.service.DriverInfoService
import sk.csirt.viruschecker.routing.GatewayRoutes

private val logger = KotlinLogging.logger { }

@KtorExperimentalLocationsAPI
fun Route.driversInfo(driverInfoService: DriverInfoService) {
    get<GatewayRoutes.DriversInfo> {
        logger.debug { "Receiving driver info" }
        val driversInfo = driverInfoService.info()
        logger.info { "Sending driver info to ${call.request.local.remoteHost}" }
        call.respond(driversInfo)
    }
}


