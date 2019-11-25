package sk.csirt.viruschecker.client.service

import io.ktor.client.engine.mock.respond
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import sk.csirt.viruschecker.client.config.jsonHeaders
import sk.csirt.viruschecker.client.config.mockHttpClient
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.DriverInfoResponse
import sk.csirt.viruschecker.routing.payload.UrlDriverInfoResponse
import sk.csirt.viruschecker.utils.json
import kotlin.test.Test
import kotlin.test.assertEquals

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class GatewayInfoServiceTest {

    private val testResponse = listOf(
        UrlDriverInfoResponse(
            url = "http://localhost:8081",
            success = true,
            info = DriverInfoResponse(
                driverVersion = "1.0.0",
                antivirus = "ClamAV, Comodo"
            )
        ),
        UrlDriverInfoResponse(
            url = "http://localhost:8082",
            success = true,
            info = DriverInfoResponse(
                driverVersion = "1.0.0",
                antivirus = "Avast, Eset, Kaspersky, Microsoft"
            )
        )
    )

    private val mockHttpClient = mockHttpClient { request ->
        when (request.url.encodedPath) {
            GatewayRoutes.driversInfo -> respond(
                content = testResponse.json(),
                headers = jsonHeaders
            )
            else -> error("Unhandled request ${request.url.encodedPath}")
        }

    }

    private val gatewayInfoService = GatewayInfoService("http://localhost:8080", mockHttpClient)

    @Test
    fun `Get info test`() = runBlockingTest {
        val response = gatewayInfoService.info()
        assertEquals(testResponse, response)
    }
}