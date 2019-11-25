package sk.csirt.viruschecker.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class HostPortTest{

    @Test
    fun `Test splitting url with port`(){
        val url = "http://192.168.0.1:8080"
        val hostPort = HostPort.fromUrlWithPort(url)
        assertEquals("192.168.0.1", hostPort.host)
        assertEquals(8080, hostPort.port)
    }

    @Test
    fun `Test splitting localhost url with port`(){
        val url = "http://localhost:8080"
        val hostPort = HostPort.fromUrlWithPort(url)
        assertEquals("localhost", hostPort.host)
        assertEquals(8080, hostPort.port)
    }

    @Test
    fun `Test to string`(){
        val hostPort = HostPort(
            host = "192.168.0.1",
            port = 8080
        )
        val hostPortString = hostPort.toString()
        assertEquals("http://192.168.0.1:8080", hostPortString)
    }
}