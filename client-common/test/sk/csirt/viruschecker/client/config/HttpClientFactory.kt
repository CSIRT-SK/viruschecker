package sk.csirt.viruschecker.client.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.headersOf
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
internal fun mockHttpClient(handler: MockRequestHandler) = HttpClient(MockEngine) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
    install(WebSockets)

    engine {
        addHandler(handler)
    }
}

internal val jsonHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))