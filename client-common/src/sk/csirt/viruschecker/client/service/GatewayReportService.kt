package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse

class GatewayReportService(
    private val gatewayUrl: String,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }

    suspend fun findReportBySha256(sha256: String): FileHashScanResponse =
        client.get<FileHashScanResponse>(
            "$gatewayUrl${GatewayRoutes.scanReport.replace("{sha256}", sha256)}"
        ).also {
            logger.info { "Retrieved report for hash $sha256" }
            logger.debug { "Retrieved report for hash $sha256: $it" }
        }

    suspend fun findAllReports(): List<FileHashScanResponse> =
        client.get<List<FileHashScanResponse>>(
            "$gatewayUrl${GatewayRoutes.allScanReports}"
        ).also {
            logger.info { "Retrieved all reports" }
        }

    suspend fun findReportsBy(searchWords: String): List<FileHashScanResponse> {
        logger.info{ "Original search words: $searchWords" }
        val searchWordsParsed = searchWords
            .replace(", ", ",")
            .replace(" ", ",")
        return client.get<List<FileHashScanResponse>>(
            "$gatewayUrl${GatewayRoutes.scanReportsBy.replace("{searchWords}", searchWordsParsed)}"
        ).also {
            logger.info { "Retrieved ${it.size} reports for search words \"$searchWordsParsed\"" }
            logger.debug { "Retrieved ${it.size} reports for search word \"$searchWordsParsed\": $it" }
        }
    }


}