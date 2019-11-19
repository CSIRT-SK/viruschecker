package sk.csirt.viruschecker.routing

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

object DriverRoutes {
    const val index = "/"

    @KtorExperimentalLocationsAPI
    @Location(index)
    class Index

    const val scanFile = "/scanFile"

    @KtorExperimentalLocationsAPI
    @Location(scanFile)
    class ScanFile

    const val scanFileWebSocket = "/ws/scanFile"
}