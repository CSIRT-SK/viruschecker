package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.coroutineScope
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.UrlDriverInfoResponse

class GatewayInfoService(
    private val gatewayUrl: String,
    private val client: HttpClient
) {
    suspend fun info(): List<UrlDriverInfoResponse> = coroutineScope {
        client.get<List<UrlDriverInfoResponse>>("$gatewayUrl${GatewayRoutes.driversInfo}")
    }
}