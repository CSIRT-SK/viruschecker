package sk.csirt.viruschecker.driver.routing

import io.ktor.routing.Route
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import mu.KotlinLogging
import sk.csirt.viruschecker.driver.antivirus.Antivirus
import sk.csirt.viruschecker.routing.payload.AntivirusDriverInfoResponse
import sk.csirt.viruschecker.routing.ApiRoutes

private val logger = KotlinLogging.logger { }


private const val ideRunMessage = "Driver application is probably running in IDE."

@KtorExperimentalLocationsAPI
fun Route.info(virusChecker: Antivirus) {
    get<ApiRoutes.Info> {
        call.respond(AntivirusDriverInfoResponse(
            antivirus = virusChecker.type.commonName,
            driverVersion = AntivirusDriverInfoResponse::class.java.`package`.implementationVersion
                ?: "Version not available. $ideRunMessage"
        ))
    }
}


