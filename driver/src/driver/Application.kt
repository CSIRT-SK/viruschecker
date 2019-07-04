package driver

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import org.koin.ktor.ext.Koin
import org.slf4j.event.Level
import driver.routing.checkFile
import driver.antivirus.Antivirus
import driver.config.*
import driver.scheduled.TimeScheduler
import mu.KotlinLogging
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

lateinit var parsedArgs: CommandLineArguments

fun main(args: Array<String>) = mainBody {
    parsedArgs = ArgParser(args).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {

    val logger = KotlinLogging.logger {  }

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

        checkFile(virusChecker)

    }


}

