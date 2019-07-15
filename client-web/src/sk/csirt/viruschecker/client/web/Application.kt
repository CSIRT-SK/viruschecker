package sk.csirt.viruschecker.client.web

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import io.ktor.application.*
import io.ktor.client.features.ClientRequestException
import io.ktor.content.TextContent
import io.ktor.request.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import org.koin.ktor.ext.Koin
import org.slf4j.event.Level
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import mu.KotlinLogging
import org.koin.ktor.ext.inject
import sk.csirt.viruschecker.client.service.DriverInfoGatewayService
import sk.csirt.viruschecker.client.service.MultiScanService
import sk.csirt.viruschecker.client.service.ReportByHashService
import sk.csirt.viruschecker.client.web.config.CommandLineArguments
import sk.csirt.viruschecker.client.web.config.webClientDependencyInjectionModule
import sk.csirt.viruschecker.client.web.routing.index
import sk.csirt.viruschecker.client.web.routing.showReport
import sk.csirt.viruschecker.client.web.routing.scanFile
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

    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
            throw it
        }
        exception<ClientRequestException> {
            call.respond(HttpStatusCode.NotFound, "Specified resource was not found")
            throw it
        }
        status(HttpStatusCode.NotFound) {
            call.respond(
                TextContent(
                    status = it,
                    text = "${it.value} ${it.description}",
                    contentType =  ContentType.Text.Plain.withCharset(Charsets.UTF_8)
                )
            )
        }
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

    // Allows to use classes annotated with @Location to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    install(Locations)

    install(Koin) {
        modules(webClientDependencyInjectionModule)
//        properties(PropertiesFactory.loadOrCreateDefault())
    }


    val scanService by inject<MultiScanService>()
    val scanReportService by inject<ReportByHashService>()
    val antivirusDriverInfoService by inject<DriverInfoGatewayService>()

    routing {
        index(antivirusDriverInfoService)
        styles()
        scanFile(scanService = scanService)
        showReport(scanReportService)
    }
}

