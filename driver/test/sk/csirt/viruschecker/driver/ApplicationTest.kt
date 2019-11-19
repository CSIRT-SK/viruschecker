package sk.csirt.viruschecker.driver

import com.xenomachina.argparser.ArgParser
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.config.filterArgsForArgParser
import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.config.CommandLineArguments
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.driver.config.testDriverDependencyInjectionModule
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
fun <R> testApplication(test: TestApplicationEngine.() -> R): R =
    withTestApplication({ module(testDriverDependencyInjectionModule) }) { this.test() }


@ExperimentalCoroutinesApi
class ApplicationTest {
    init {
        parsedArgs = ArgParser(
            filterArgsForArgParser(
                arrayOf(
                    AntivirusType.DUMMY.name
                )
            )
        ).parseInto(::CommandLineArguments)
    }

    @Test
    fun testRoot() {
        testApplication {
            handleRequest(HttpMethod.Get, DriverRoutes.index).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
