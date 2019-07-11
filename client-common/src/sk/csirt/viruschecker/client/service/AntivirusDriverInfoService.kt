package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.ApiRoutes
import sk.csirt.viruschecker.routing.payload.AntivirusDriverInfoResponse

class AntivirusDriverInfoService (
    driverUrls: List<String>,
    client: HttpClient
): AntivirusDriverService(driverUrls, client){
    private val logger = KotlinLogging.logger {  }
    suspend fun info(): List<AntivirusDriverInfoResponse> = multiDriverRequest{ driverUrl, client ->
        client.get<AntivirusDriverInfoResponse>("$driverUrl${ApiRoutes.info}")
    }

}