package sk.csirt.viruschecker.gateway.routing.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.DriverInfoResponse
import sk.csirt.viruschecker.routing.payload.UrlDriverInfoResponse

class DriverInfoService(
    driverUrls: List<String>,
    client: HttpClient
) : AntivirusDriverService(driverUrls, client) {

    suspend fun info(): List<UrlDriverInfoResponse> =
        multiDriverRequest { driverUrl, client ->
            val info = client.get<DriverInfoResponse>("$driverUrl${DriverRoutes.index}")
            UrlDriverInfoResponse(
                url = driverUrl,
                success = true,
                info = info
            )
        }.map { (driverUrl, result) ->
            result.getOrDefault(
                UrlDriverInfoResponse(
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