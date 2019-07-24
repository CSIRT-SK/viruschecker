package sk.csirt.viruschecker.gateway.routing

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import sk.csirt.viruschecker.gateway.routing.service.FileScanService
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.GatewayInfoResponse

private const val ideRunMessage = "Version not available. Gateway application is probably running from IDE."

@KtorExperimentalLocationsAPI
fun Route.index() {
    get<GatewayRoutes.Index> {
        call.respond(
            GatewayInfoResponse(
                gatewayVersion = FileScanService::class.java.`package`.implementationVersion ?: ideRunMessage
            )
        )
    }
}