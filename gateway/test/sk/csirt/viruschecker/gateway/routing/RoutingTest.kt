package sk.csirt.viruschecker.gateway.routing

import com.xenomachina.argparser.ArgParser
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respondOk
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.config.filterArgsForArgParser
import sk.csirt.viruschecker.gateway.config.CommandLineArguments
import sk.csirt.viruschecker.gateway.config.testGatewayDependencyInjectionModule
import sk.csirt.viruschecker.gateway.module
import sk.csirt.viruschecker.gateway.parsedArgs

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal fun <R> createTestApplication(
    httpClientRequestHandler: MockRequestHandler = { respondOk() },
    test: TestApplicationEngine.() -> R
): R =
    withTestApplication({
        module(
            testGatewayDependencyInjectionModule(
                httpClientRequestHandler = httpClientRequestHandler
            )
        )
    }) { this.test() }

internal val testDriverUrl = "http://localhost:8081"

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal abstract class RoutingTest {
    init {
        val testDriverUrlsFile = createTempFile()
        testDriverUrlsFile.writeText(testDriverUrl)
        parsedArgs = ArgParser(
            filterArgsForArgParser(
                arrayOf(
                    testDriverUrlsFile.path
                )
            )
        ).parseInto(::CommandLineArguments)
    }
}