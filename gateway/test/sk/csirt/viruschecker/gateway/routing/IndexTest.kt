package sk.csirt.viruschecker.gateway.routing

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.routing.payload.GatewayInfoResponse
import sk.csirt.viruschecker.utils.fromJson
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class IndexTest : RoutingTest() {
    @Test
    fun `Index test`() {
        createTestApplication {
            handleRequest(HttpMethod.Get, GatewayRoutes.index).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content?.fromJson<GatewayInfoResponse>())
            }
        }
    }
}