package sk.csirt.viruschecker.client.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import io.ktor.application.Application
import kotlinx.coroutines.runBlocking
import sk.csirt.viruschecker.client.cli.config.CommandLineArguments
import sk.csirt.viruschecker.client.config.httpClient
import sk.csirt.viruschecker.client.reporting.CommandLineReporter
import sk.csirt.viruschecker.client.reporting.CsvReporter
import sk.csirt.viruschecker.client.reporting.DefaultReporter
import sk.csirt.viruschecker.client.reporting.Reporter
import sk.csirt.viruschecker.client.service.ClientScanService
import sk.csirt.viruschecker.client.service.ScanParameters
import sk.csirt.viruschecker.config.filterArgsForArgParser
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import sk.csirt.viruschecker.utils.JsonConverter
import java.time.Instant
import kotlin.system.exitProcess

lateinit var parsedArgs: CommandLineArguments

fun main(args: Array<String>) = mainBody {
    parsedArgs = ArgParser(filterArgsForArgParser(args)).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val client = httpClient(parsedArgs.socketTimeout)

    val gatewayUrl = parsedArgs.gateway
    val fileToScan = parsedArgs.fileToScan

    val scanService = ClientScanService(gatewayUrl, client, JsonConverter())
//    val scanReport = runBlocking {
//        scanService.scanFile(
//            ScanParameters(
//                fileToScan, fileToScan.name,
//                parsedArgs.useExternalDrivers
//            )
//        )
//    }
    val scanReport = runBlocking {
        val reports = mutableListOf<AntivirusReportResponse>()
        scanService.scanFileWebSocket(
            ScanParameters(
                fileToScan, fileToScan.name,
                parsedArgs.useExternalDrivers
            )
        ) {
            reports += it
        }
        FileHashScanResponse(
            md5 = "ha",
            sha1 = "haha",
            sha256 = "hahaha",
            report = FileScanResponse(
                date = Instant.now(),
                filename = fileToScan.name,
                results = reports
            )
        )
    }

    printReports(scanReport)

    client.close()
    exitProcess(0)
}

private fun printReports(scanReport: FileHashScanResponse) {
    val reportFile = parsedArgs.outputFile

    val reporters: List<Reporter> = listOf<Reporter>(
        CommandLineReporter()
    ) + if (reportFile != null) {
        if (reportFile.name.endsWith(".csv"))
            listOf<Reporter>(CsvReporter(reportFile))
        else
            listOf<Reporter>(DefaultReporter(reportFile))
    } else emptyList()


    reporters.forEach { it.saveReport(scanReport) }
}


