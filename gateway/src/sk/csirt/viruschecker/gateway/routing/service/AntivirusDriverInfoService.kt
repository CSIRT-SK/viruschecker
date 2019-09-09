package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.DriverInfoResponse
import sk.csirt.viruschecker.routing.payload.UrlAntivirusDriverInfoResponse

class DriverInfoService(
    driverUrls: List<String>,
    client: HttpClient
) : AntivirusDriverService(driverUrls, client) {

    private val logger = KotlinLogging.logger { }

        suspend fun info(): List<UrlAntivirusDriverInfoResponse> =
        multiDriverRequest { driverUrl, client ->
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
                        antivirus = "NA"
                    )
                )
            )
        }
}