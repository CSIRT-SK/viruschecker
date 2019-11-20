package sk.csirt.viruschecker.driver.routing

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.routing.DriverRoutes
import kotlin.test.Test
import kotlin.test.assertEquals

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class IndexTest : RoutingTest() {
    @Test
    fun testIndex() {
        createTestApplication {
            handleRequest(HttpMethod.Get, DriverRoutes.index).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
