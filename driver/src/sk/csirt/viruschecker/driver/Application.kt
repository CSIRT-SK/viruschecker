package sk.csirt.viruschecker.driver

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
import sk.csirt.viruschecker.driver.antivirus.Antivirus
import sk.csirt.viruschecker.driver.config.*
import io.ktor.http.ContentType
import mu.KotlinLogging
import org.koin.ktor.ext.inject
import sk.csirt.viruschecker.driver.config.CommandLineArguments
import sk.csirt.viruschecker.driver.config.PropertiesFactory
import sk.csirt.viruschecker.driver.config.driverDependencyInjectionModule
import sk.csirt.viruschecker.driver.routing.scanFile

private val logger = KotlinLogging.logger {  }

lateinit var parsedArgs: CommandLineArguments

fun main(args: Array<String>) = mainBody {
    parsedArgs = ArgParser(args).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {

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

    install(Koin) {
        modules(driverDependencyInjectionModule)
        properties(PropertiesFactory.loadOrCreateDefault())
    }

//    val schedulers = listOf<TimeScheduler>(
//        get(updateScheduler),
//        get(cleanScheduler)
//    )
//    logger.info("Registered time schedulers : $schedulers")

    val virusChecker by inject<Antivirus>(parsedArgs.antivirus)

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

        scanFile(virusChecker)

    }


}

