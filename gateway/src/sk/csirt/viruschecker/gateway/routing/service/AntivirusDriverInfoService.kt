package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.config.Drivers
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.DriverInfoResponse
import sk.csirt.viruschecker.routing.payload.UrlAntivirusDriverInfoResponse

class DriverInfoService(
    driverUrls: List<String>,
    client: HttpClient
) : AntivirusDriverService(Drivers(internal = driverUrls, external = emptyList()), client) {
    private val logger = KotlinLogging.logger { }
    suspend fun info(): List<UrlAntivirusDriverInfoResponse> =
        // All drivers are here treated as internal, i.e. they do not use the external services.
        multiDriverRequest(useExternalDrivers = true) { driverUrl, client ->
            val info = client.get<DriverInfoResponse>("$driverUrl${DriverRoutes.index}")
            UrlAntivirusDriverInfoResponse(
                url = driverUrl,
                success = true,
                info = info
            )
        }.map { (driverUrl, result) ->
            result.getOrDefault(
                UrlAntivirusDriverInfoResponse(
                    url = driverUrl,
                    success = false,
                    info = DriverInfoResponse(
                        driverVersion = "ERROR: Could not reach driver.",
                        usesExternalServices = false,
                        antivirus = "NA"
                    )
                )
            )
        }
}