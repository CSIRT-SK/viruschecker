package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.payload.UrlAntivirusDriverInfoResponse
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.AntivirusDriverInfoResponse

class DriverInfoGatewayService(
    private val gatewayUrl: String,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }
    suspend fun info(): List<UrlAntivirusDriverInfoResponse> = coroutineScope {
        client.get<List<UrlAntivirusDriverInfoResponse>>("$gatewayUrl${GatewayRoutes.driversInfo}")
    }
}