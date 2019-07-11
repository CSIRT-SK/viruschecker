package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.call
import io.ktor.http.CacheControl
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.routing.Routing
import kotlinx.html.*
import sk.csirt.viruschecker.client.service.AntivirusDriverInfoService
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml

@KtorExperimentalLocationsAPI
fun Routing.index(antivirusDriverInfoService: AntivirusDriverInfoService) {
    get<WebRoutes.Index> {
        val version = WebRoutes.Index::class.java.`package`.implementationVersion
            ?: "Not available, run the app as JAR"
        val info = antivirusDriverInfoService.info()
        call.respondDefaultHtml(visibility = CacheControl.Visibility.Public) {
            h2 { +"Welcome" }
            +"This is a web interface of the VirusChecker, version: $version."

            h3 { +"These are currently deployed antivirus drivers." }
            info.forEach {
                p{
                    +"Antivirus: ${it.antivirus}"
                    br()
                    +"Driver version: ${it.driverVersion}"
                }
            }
        }
    }
}