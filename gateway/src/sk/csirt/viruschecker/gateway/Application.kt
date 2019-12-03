package sk.csirt.viruschecker.gateway

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
import mu.KotlinLogging
import org.koin.core.module.Module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import sk.csirt.viruschecker.config.filterArgsForArgParser
import sk.csirt.viruschecker.gateway.config.CommandLineArguments
import sk.csirt.viruschecker.gateway.config.checkedDriverUrls
import sk.csirt.viruschecker.gateway.config.gatewayDependencyInjectionModule
import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
import sk.csirt.viruschecker.gateway.routing.*
import sk.csirt.viruschecker.gateway.routing.service.CachedDriverScanService
import sk.csirt.viruschecker.routing.payload.UrlDriverInfoResponse

lateinit var parsedArgs: CommandLineArguments
val logger = KotlinLogging.logger{ }

fun main(args: Array<String>) = mainBody {
    parsedArgs = ArgParser(filterArgsForArgParser(args)).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module(dependencyInjection: Module = gatewayDependencyInjectionModule(
    driverUrls =  parsedArgs.driverUrls,
    defaultSocketTimeout = parsedArgs.socketTimeout,
    isPersistedDatabase = parsedArgs.useInMemoryDatabase.not(),
    config = environment.config
)) {

    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
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
        fileProperties()
    }

    install(WebSockets){
        timeout = parsedArgs.socketTimeout
    }

    val scanService by inject<CachedDriverScanService>()
    val scanReportService by inject<PersistentScanReportService>()
    val checkedUrls by inject<List<UrlDriverInfoResponse>>(checkedDriverUrls)
//    val shareService by inject<ShareService>()

    routing {
        index()
        driversInfo(checkedUrls)
        multiScanFile(scanService)
        findByHash(scanReportService)
//        shareFile(shareService)
        findBy(scanReportService)
        findAll(scanReportService)
    }


}

