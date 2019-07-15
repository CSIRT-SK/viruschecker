package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.payload.UrlAntivirusDriverInfoResponse
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.AntivirusDriverInfoResponse

class DriverInfoService(
    driverUrls: List<String>,
    client: HttpClient
) : AntivirusDriverService(driverUrls, client) {
    private val logger = KotlinLogging.logger { }
    suspend fun info(): List<UrlAntivirusDriverInfoResponse> =
        multiDriverRequest { driverUrl, client ->
            try {
                val info = client.get<AntivirusDriverInfoResponse>("$driverUrl${DriverRoutes.index}")
                UrlAntivirusDriverInfoResponse(
                    url = driverUrl,
                    success = true,
                    info = info
                )
            } catch (e: Exception) {
                UrlAntivirusDriverInfoResponse(
                    url = driverUrl,
                    success = false,
                    info = AntivirusDriverInfoResponse(
                        driverVersion = "ERROR: Could not reach driver.",
                        antivirus = "NA"
                    )
                )
            }
        }

}