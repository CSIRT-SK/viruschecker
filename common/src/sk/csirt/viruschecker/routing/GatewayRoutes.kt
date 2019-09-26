package sk.csirt.viruschecker.routing

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

object GatewayRoutes {
    const val multiScanFile = "/multiScanFile"
    @KtorExperimentalLocationsAPI
    @Location(multiScanFile)
    class MultiScanFile

    const val shareFile = "/shareFile"
    @KtorExperimentalLocationsAPI
    @Location(shareFile)
    class ShareFile

    const val driversInfo = "/driversInfo"
    @KtorExperimentalLocationsAPI
    @Location(driversInfo)
    class DriversInfo

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
}