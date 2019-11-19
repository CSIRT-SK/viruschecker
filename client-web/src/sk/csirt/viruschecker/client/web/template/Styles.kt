package sk.csirt.viruschecker.client.web.template

import io.ktor.application.call
import io.ktor.http.content.resolveResource
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import sk.csirt.viruschecker.client.web.routing.WebRoutes


/**
 * Register the styles, [WebRoutes.MainCss] route (/styles/main.css)
 */
@KtorExperimentalLocationsAPI
fun Route.styles() {
    /**
     * On a GET request to the [WebRoutes.MainCss] route, it returns the `theme.css` file from the resources.
     *
     * Here we could preprocess or join several CSS/SASS/LESS.
     */
    get<WebRoutes.MainCss> {
        call.respond(call.resolveResource("theme.css")!!)
    }
}