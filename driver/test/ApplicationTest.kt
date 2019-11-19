package sk.csirt.viruschecker

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.sk.csirt.viruschecker.driver.AbstractTest
import sk.csirt.viruschecker.sk.csirt.viruschecker.driver.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ApplicationTest : AbstractTest() {

    @Test
    fun testRoot() {
        testApplication {
            handleRequest(HttpMethod.Get, DriverRoutes.index).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
