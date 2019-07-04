package gateway

import gateway.config.CommandLineArguments
import gateway.payload.AntivirusResponse
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.*
import io.ktor.http.content.PartData
import kotlinx.coroutines.runBlocking
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream

private val logger = KotlinLogging.logger {  }

lateinit var parsedArgs: CommandLineArguments

fun main(args: Array<String>) {
//    parsedArgs = ArgParser(args).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
//@kotlin.jvm.JvmOverloads
fun Application.module() {
    val client = HttpClient(Apache) {
        install(Logging) {
            level = LogLevel.HEADERS
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        engine {
            socketTimeout = 40_000
            connectTimeout = 40_000
            connectionRequestTimeout = 80_000
        }
    }



//    val url = parsedArgs.driverUrls.first()
//    val url = "http://localhost:8080"
// Avast
    val url = "http://192.168.1.113:8080"
// Eset
//    val url = "http://192.168.1.111:8080"
// Kaspersky
//val url = "http://192.168.1.115:8080"

//    val fileToScan = parsedArgs.fileToScan

//     val fileToScan = File("gradle.properties")
//    val fileToScan = File("go.zip")
    val fileToScan = File("eicar.exe")
//     val fileToScan = File("goWEicar.zip")

    runBlocking {
        client.post<AntivirusResponse>("$url/scanFile") {
            this.body = MultiPartFormDataContent(
                listOf(
                    PartData.FileItem(
                        partHeaders = Headers.build {
                            this[HttpHeaders.ContentDisposition] =
                                ContentDisposition.File.withParameter("filename", fileToScan.name).toString()
                        },
                        dispose = {  },
                        provider = { FileInputStream(fileToScan).asInput() }
                    )
                )
            )
        }.also {
            logger.info("Retrieved report: $it")
        }
    }


}

