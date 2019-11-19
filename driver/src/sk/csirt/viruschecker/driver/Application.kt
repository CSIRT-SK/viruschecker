package sk.csirt.viruschecker.driver

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.module.Module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import sk.csirt.viruschecker.config.filterArgsForArgParser
import sk.csirt.viruschecker.driver.antivirus.Antivirus
import sk.csirt.viruschecker.driver.config.CommandLineArguments
import sk.csirt.viruschecker.driver.config.DriverPropertiesFactory
import sk.csirt.viruschecker.driver.config.defaultAntivirusQualifier
import sk.csirt.viruschecker.driver.config.driverDependencyInjectionModule
import sk.csirt.viruschecker.driver.routing.index
import sk.csirt.viruschecker.driver.routing.scanFile

lateinit var parsedArgs: CommandLineArguments

private val viruscheckerDriverProperties = DriverPropertiesFactory.loadOrCreateDefault()

fun main(args: Array<String>) = mainBody {
    parsedArgs = ArgParser(filterArgsForArgParser(args)).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module(dependencyInjection: Module = driverDependencyInjectionModule) {

    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
    }

    install(WebSockets){
        timeout = parsedArgs.socketTimeout
    }

//    install(CORS) {
//        method(HttpMethod.Options)
//        method(HttpMethod.Put)
//        method(HttpMethod.Delete)
//        method(HttpMethod.Patch)
//        header(HttpHeaders.Authorization)
//        header("MyCustomHeader")
//        allowCredentials = true
//        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
//    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(Locations)
    install(Koin) {
        modules(dependencyInjection)
        properties(viruscheckerDriverProperties)
    }

    val antivirus by inject<Antivirus>(defaultAntivirusQualifier)

    routing {
        index(antivirus)
        scanFile(antivirus)
    }
}