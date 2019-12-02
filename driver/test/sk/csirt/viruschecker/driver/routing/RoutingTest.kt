package sk.csirt.viruschecker.driver.routing

import com.xenomachina.argparser.ArgParser
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.config.filterArgsForArgParser
import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.config.CommandLineArguments
import sk.csirt.viruschecker.driver.config.testDriverDependencyInjectionModule
import sk.csirt.viruschecker.driver.module
import sk.csirt.viruschecker.driver.parsedArgs

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal fun <R> createTestApplication(test: TestApplicationEngine.() -> R): R =
    withTestApplication({ module(testDriverDependencyInjectionModule) }) { this.test() }

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal abstract class RoutingTest {
    init {
        parsedArgs = ArgParser(
            filterArgsForArgParser(
                arrayOf(
                    AntivirusType.DUMMY.name
                )
            )
        ).parseInto(::CommandLineArguments)
    }
}
