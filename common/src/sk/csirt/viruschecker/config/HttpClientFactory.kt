package sk.csirt.viruschecker.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.websocket.WebSockets
import io.ktor.util.KtorExperimentalAPI
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.X509TrustManager

@KtorExperimentalAPI
fun httpClient(defaultSocketTimeout: Duration) = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
    install(Logging) {
        level = LogLevel.HEADERS
    }
    install(WebSockets)
    engine {
        endpoint {
            connectTimeout = defaultSocketTimeout.toMillis()
            requestTimeout = defaultSocketTimeout.toMillis()
        }
        https {
            trustManager = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate>? {
                    return null
                }
            }
        }
    }
}