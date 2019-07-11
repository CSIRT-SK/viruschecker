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

    const val index = "/"
    @KtorExperimentalLocationsAPI
    @Location(index)
    class Index()

    const val scanReport = "/scanReport/{id}"
    @KtorExperimentalLocationsAPI
    @Location(scanReport)
    data class ScanReport(val id: String)
}