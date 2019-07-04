//package sk.csirt.viruschecker
//
//import driver.module
//import io.ktor.http.*
//import kotlin.test.*
//import io.ktor.server.testing.*
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
