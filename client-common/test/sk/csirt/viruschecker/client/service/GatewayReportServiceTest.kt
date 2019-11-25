package sk.csirt.viruschecker.client.service

import io.ktor.client.engine.mock.respond
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import sk.csirt.viruschecker.client.config.jsonHeaders
import sk.csirt.viruschecker.client.config.mockHttpClient
import sk.csirt.viruschecker.client.testScanResponse
import sk.csirt.viruschecker.client.testScanResponseList
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.utils.json
import kotlin.test.Test
import kotlin.test.assertEquals

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class GatewayReportServiceTest {

    private val testSearchWord = testScanResponseList.first().report.filename

    private val testFoundBySearchWord = testScanResponseList.filter { it.report.filename == testSearchWord }

    private val mockHttpClient = mockHttpClient { request ->
        when (request.url.encodedPath) {
            GatewayRoutes.scanReport.replace("{sha256}", testScanResponse.sha256) -> respond(
                content = testScanResponse.json(),
                headers = jsonHeaders
            )

            GatewayRoutes.allScanReports -> respond(
                content = testScanResponseList.json(),
                headers = jsonHeaders
            )

            GatewayRoutes.scanReportsBy.replace("{searchWords}", testSearchWord) -> respond(
                content = testFoundBySearchWord.json(),
                headers = jsonHeaders
            )

            else -> error("Unhandled request ${request.url.encodedPath}")
        }
    }

    private val gatewayReportService = GatewayReportService("http://localhost:8080", mockHttpClient)

    @Test
    fun `Find report by SHA-256 test`() = runBlockingTest {
        val response = gatewayReportService.findReportBySha256(testScanResponse.sha256)
        assertEquals(testScanResponse, response)
    }

    @Test
    fun `Find all reports test`() = runBlockingTest {
        val response = gatewayReportService.findAllReports()
        assertEquals(testScanResponseList, response)
    }

    @Test
    fun `Find reports by search word test`() = runBlockingTest {
        val response = gatewayReportService.findReportsBy(testSearchWord)
        assertEquals(testFoundBySearchWord, response)
    }
}