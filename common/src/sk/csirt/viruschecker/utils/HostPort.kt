package sk.csirt.viruschecker.utils

import io.ktor.http.DEFAULT_PORT

data class HostPort(
    val host: String,
    val port: Int
) {
    companion object {
        fun fromUrlWithPort(url: String): HostPort {
            val gatewayHostPort = url.split(":")
            val host = gatewayHostPort[1].substring(2)
            val port = gatewayHostPort.getOrNull(2)?.toIntOrNull() ?: DEFAULT_PORT
            return HostPort(host, port)
        }
    }

    override fun toString(): String = "http://$host:$port"
}