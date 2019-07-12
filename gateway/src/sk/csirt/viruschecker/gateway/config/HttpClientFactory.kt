package sk.csirt.viruschecker.gateway.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging

fun httpClient(defaultSocketTimeout: Int) = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
    install(Logging) {
        level = LogLevel.HEADERS
    }
    engine {
        socketTimeout = defaultSocketTimeout
        connectTimeout = defaultSocketTimeout
        connectionRequestTimeout = defaultSocketTimeout
    }
}
