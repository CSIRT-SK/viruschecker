package sk.csirt.viruschecker.client.web

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.gson.*
import org.koin.ktor.ext.Koin
import org.slf4j.event.Level
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import mu.KotlinLogging
import org.koin.ktor.ext.inject
import sk.csirt.viruschecker.client.service.AntivirusDriverInfoService
import sk.csirt.viruschecker.client.service.DefaultScanService
import sk.csirt.viruschecker.client.web.config.CommandLineArguments
import sk.csirt.viruschecker.client.web.config.webClientDependencyInjectionModule
import sk.csirt.viruschecker.client.web.routing.index
import sk.csirt.viruschecker.client.web.routing.showReport
import sk.csirt.viruschecker.client.web.routing.scanFile
import sk.csirt.viruschecker.client.web.service.ScanReportService
import sk.csirt.viruschecker.client.web.template.styles

private val logger = KotlinLogging.logger { }

lateinit var parsedArgs: CommandLineArguments

fun main(args: Array<String>) = mainBody {
    parsedArgs = ArgParser(args).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
    }
    // Automatic '304 Not Modified' Responses
    install(ConditionalHeaders)

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

    // Allows to use classes annotated with @Location to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    install(Locations)

    install(Koin) {
        modules(webClientDependencyInjectionModule)
//        properties(PropertiesFactory.loadOrCreateDefault())
    }


    val scanService by inject<DefaultScanService>()
    val scanReportService by inject<ScanReportService>()
    val antivirusDriverInfoService by inject<AntivirusDriverInfoService>()

    routing {
//        get("/") {
//            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
//            call.respondRedirect(call.url(WebRoutes.scanFile), permanent = false)
//        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

        index(antivirusDriverInfoService)
        styles()
        scanFile(scanService = scanService, scanReportService = scanReportService)
        showReport(scanReportService)

    }


}

