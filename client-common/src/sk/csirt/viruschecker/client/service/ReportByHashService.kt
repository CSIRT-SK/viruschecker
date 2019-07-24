package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse

class ReportByHashService(
    private val gatewayUrl: String,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    suspend fun findReportBySha256(sha256: String): FileHashScanResponse =
        client.get("$gatewayUrl${GatewayRoutes.scanReport.replace("{sha256}", sha256)}")
}