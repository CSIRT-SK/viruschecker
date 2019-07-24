package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import mu.KotlinLogging
import sk.csirt.viruschecker.gateway.config.Drivers
import sk.csirt.viruschecker.routing.payload.UrlAntivirusDriverInfoResponse
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.AntivirusDriverInfoResponse

class DriverInfoService(
    driverUrls: Drivers,
    client: HttpClient
) : AntivirusDriverService(driverUrls, client) {
    private val logger = KotlinLogging.logger { }
    suspend fun info(): List<UrlAntivirusDriverInfoResponse> =
        multiDriverRequest(useExternalDrivers = true) { driverUrl, client ->
            val info = client.get<AntivirusDriverInfoResponse>("$driverUrl${DriverRoutes.index}")
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
                    info = AntivirusDriverInfoResponse(
                        driverVersion = "ERROR: Could not reach driver.",
                        antivirus = "NA"
                    )
                )
            )
        }
}