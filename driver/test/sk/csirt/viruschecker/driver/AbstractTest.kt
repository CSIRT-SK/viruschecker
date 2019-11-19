package sk.csirt.viruschecker.sk.csirt.viruschecker.driver

import com.xenomachina.argparser.ArgParser
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.config.filterArgsForArgParser
import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.config.CommandLineArguments
import sk.csirt.viruschecker.driver.module
import sk.csirt.viruschecker.driver.parsedArgs
import sk.csirt.viruschecker.sk.csirt.viruschecker.driver.config.testDriverDependencyInjectionModule

abstract class AbstractTest {
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

@ExperimentalCoroutinesApi
fun <R> testApplication(test: TestApplicationEngine.() -> R): R =
    withTestApplication({ module(testDriverDependencyInjectionModule) }) { this.test() }
