package sk.csirt.viruschecker.client.web.routing

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

object WebRoutes {
    const val mainCss = "/styles/main.css"
    @KtorExperimentalLocationsAPI
    @Location(mainCss)
    class MainCss

    const val scanFile = "/scanFile"
    @KtorExperimentalLocationsAPI
    @Location(scanFile)
    class ScanFile

    const val shareFile = "/shareFile"
    @KtorExperimentalLocationsAPI
    @Location(shareFile)
    class ShareFile

    const val index = "/"
    @KtorExperimentalLocationsAPI
    @Location(index)
    class Index

    const val scanReport = "/scanReport/{sha256}"
    @KtorExperimentalLocationsAPI
    @Location(scanReport)
    data class ScanReport(val sha256: String)

    const val allScanReports = "/allScanReports"
    @KtorExperimentalLocationsAPI
    @Location(allScanReports)
    class AllScanReports

    const val scanReportsBy = "/scanReportBy"
    @KtorExperimentalLocationsAPI
    @Location(scanReportsBy)
    data class ScanReportsBy(val search: String)
}