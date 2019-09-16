package sk.csirt.viruschecker.client.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.ssl.SSLContexts
import java.time.Duration

fun httpClient(defaultSocketTimeout: Duration) = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
    install(Logging) {
        level = LogLevel.HEADERS
    }
    engine {
        socketTimeout = defaultSocketTimeout.toMillis().toInt()
        connectTimeout = defaultSocketTimeout.toMillis().toInt()
        connectionRequestTimeout = defaultSocketTimeout.toMillis().toInt()
    }
    engine {
        customizeClient {
            sslContext = SSLContexts
                .custom()
                .loadTrustMaterial(TrustSelfSignedStrategy())
                .build()
            setSSLHostnameVerifier(NoopHostnameVerifier())
        }
    }
}
