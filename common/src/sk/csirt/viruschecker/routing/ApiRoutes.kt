package sk.csirt.viruschecker.routing

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

object ApiRoutes{
    const val info = "/info"
    @KtorExperimentalLocationsAPI
    @Location(info)
    class Info

    const val scanFile = "/scanFile"
    @KtorExperimentalLocationsAPI
    @Location(scanFile)
    class ScanFile
}