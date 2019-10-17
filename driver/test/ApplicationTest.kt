//package sk.csirt.viruschecker
//
//import io.ktor.http.HttpMethod
//import io.ktor.http.HttpStatusCode
//import io.ktor.server.testing.handleRequest
//import io.ktor.server.testing.withTestApplication
//import sk.csirt.viruschecker.driver.module
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class ApplicationTest {
//    @Test
//    fun testRoot() {
//        withTestApplication({ module() }) {
//            handleRequest(HttpMethod.Get, "/").apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("HELLO WORLD!", response.content)
//            }
//        }
//    }
//}
