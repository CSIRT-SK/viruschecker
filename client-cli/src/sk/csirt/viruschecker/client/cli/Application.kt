package sk.csirt.viruschecker.client.cli

import com.xenomachina.argparser.ArgParser
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.io.streams.asInput
import mu.KotlinLogging
import reporting.CsvReporter
import sk.csirt.viruschecker.client.cli.config.CommandLineArguments
import sk.csirt.viruschecker.client.cli.payload.FileScanResponse
import sk.csirt.viruschecker.client.cli.reporting.DefaultReporter
import sk.csirt.viruschecker.client.cli.service.ScanService
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

lateinit var parsedArgs: CommandLineArguments

fun main(args: Array<String>) {
    parsedArgs = ArgParser(args).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
        engine {
            socketTimeout = 40_000
            connectTimeout = 40_000
            connectionRequestTimeout = 80_000
        }
    }

    val url = parsedArgs.gatewayUrl
    val fileToScan = parsedArgs.fileToScan

    val scanService = ScanService(url, client)
    val scanReport = scanService.scanFile(fileToScan)

    val reportFile = parsedArgs.outputFile
    if (reportFile != null) {
        val reporter = if (reportFile.name.endsWith(".csv"))
            CsvReporter()
        else
            DefaultReporter<FileScanResponse>()

        reporter.saveReport(reportFile, listOf(scanReport))
    }
}


