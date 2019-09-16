package sk.csirt.viruschecker.client.web.routing

import io.ktor.application.call
import io.ktor.http.CacheControl
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.routing.Routing
import kotlinx.html.br
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.p
import sk.csirt.viruschecker.client.service.GatewayInfoService
import sk.csirt.viruschecker.client.web.template.respondDefaultHtml

@KtorExperimentalLocationsAPI
fun Routing.index(antivirusDriverInfoService: GatewayInfoService) {
    get<WebRoutes.Index> {
        val version = WebRoutes.Index::class.java.`package`.implementationVersion
            ?: "Not available, run the app as JAR"
        val info = antivirusDriverInfoService.info()
        call.respondDefaultHtml(visibility = CacheControl.Visibility.Public) {
            h2 { +"Welcome" }
            +"This is a web interface of the VirusChecker, version: $version."

            h3 { +"These are the currently deployed antivirus drivers." }
            info.forEach {
                p{
                    +"Antivirus: ${it.info.antivirus}"
                    br()
                    +"Driver version: ${it.info.driverVersion}"
                }
            }
        }
    }
}