package sk.csirt.viruschecker.client.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sk.csirt.viruschecker.client.cli.config.CommandLineArguments
import sk.csirt.viruschecker.client.reporting.CommandLineReporter
import sk.csirt.viruschecker.client.reporting.CsvReporter
import sk.csirt.viruschecker.client.reporting.FileReporter
import sk.csirt.viruschecker.client.reporting.Reporter
import sk.csirt.viruschecker.client.service.GatewayScanService
import sk.csirt.viruschecker.client.service.ScanParameters
import sk.csirt.viruschecker.config.filterArgsForArgParser
import sk.csirt.viruschecker.config.httpClient
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import java.time.Instant

lateinit var parsedArgs: CommandLineArguments

private val logger = KotlinLogging.logger{ }

@KtorExperimentalAPI
fun main(args: Array<String>) = mainBody {
    parsedArgs = ArgParser(filterArgsForArgParser(args)).parseInto(::CommandLineArguments)
    val client = httpClient(parsedArgs.socketTimeout)

    val gatewayUrl = parsedArgs.gateway
    val fileToScan = parsedArgs.fileToScan

    val scanService = GatewayScanService(gatewayUrl, client)
    logger.debug { "Initializing scan" }
    val scanReport = runBlocking {
        val reports = mutableListOf<AntivirusReportResponse>()
        var md5 = ""
        var sha1 = ""
        var sha256 = ""
        scanService.scanFileWebSocket(
            ScanParameters(
                fileToScan, fileToScan.name,
                parsedArgs.useExternalDrivers
            ),
            {
                md5 = it.md5
                sha1 = it.sha1
                sha256 = it.sha256
            }
        ) {
            reports += it
        }
        FileHashScanResponse(
            md5 = md5,
            sha1 = sha1,
            sha256 = sha256,
            report = FileScanResponse(
                date = Instant.now(),
                filename = fileToScan.name,
                results = reports
            )
        )
    }
    logger.debug { "Printing reports" }
    printReports(scanReport)

    logger.debug { "Closing http client" }
    client.close()
    logger.debug { "Exiting process" }
}

private fun printReports(scanReport: FileHashScanResponse) {
    val reportFile = parsedArgs.outputFile

    val reporters: List<Reporter> = listOf<Reporter>(
        CommandLineReporter()
    ) + if (reportFile != null) {
        if (reportFile.name.endsWith(".csv"))
            listOf<Reporter>(CsvReporter(reportFile))
        else
            listOf<Reporter>(FileReporter(reportFile))
    } else emptyList()


    reporters.forEach { it.saveReport(scanReport) }
}