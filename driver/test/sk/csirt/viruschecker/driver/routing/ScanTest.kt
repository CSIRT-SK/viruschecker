package sk.csirt.viruschecker.driver.routing

import io.ktor.http.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.PartData
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.streams.asInput
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import sk.csirt.viruschecker.routing.payload.ScanFileWebSocketParameters
import sk.csirt.viruschecker.routing.payload.ScanStatus
import sk.csirt.viruschecker.utils.fromJson
import sk.csirt.viruschecker.utils.json
import java.io.FileInputStream
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class ScanTest : RoutingTest() {

    private val testFilename = "Cizicek-cizicek-vtacik-malicky.txt"

    @Test
    fun `Scan test`() {
        val testFile = createTempFile()
        testFile.writeText("Tento jednotkovy test venujem svojej manzelke Miroslave.")

        createTestApplication {
            handleRequest(HttpMethod.Post, DriverRoutes.scanFile) {
                val multipartBoundary = addMultiPartBoundaryHeader()
                setBody(
                    boundary = multipartBoundary,
                    parts = listOf(
                        PartData.FormItem(
                            value = true.toString(),
                            dispose = { },
                            partHeaders = Headers.Empty
                        ),
                        PartData.FileItem(
                            partHeaders = Headers.build {
                                this[HttpHeaders.ContentDisposition] =
                                    ContentDisposition.File.withParameter(
                                        "filename",
                                        testFilename
                                    ).toString()
                            },
                            dispose = { },
                            provider = { FileInputStream(testFile).asInput() }
                        )
                    )
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val responseParsed = response.content?.fromJson<FileScanResponse>()
                assertEquals(ScanStatus.OK, responseParsed?.status)
            }
        }
    }

    @Test
    fun `Scan with bad request test`() {
        val testFile = createTempFile()
        testFile.writeText("Tento jednotkovy test venujem svojej dcerke Miriam.")

        createTestApplication {
            handleRequest(HttpMethod.Post, DriverRoutes.scanFile) {
                val multipartBoundary = addMultiPartBoundaryHeader()
                setBody(
                    boundary = multipartBoundary,
                    parts = listOf(
                        PartData.FormItem(
                            value = true.toString(),
                            dispose = { },
                            partHeaders = Headers.Empty
                        )
                    )
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    private fun TestApplicationRequest.addMultiPartBoundaryHeader(): String {
        val multipartBoundary = UUID.randomUUID().toString()
        addHeader(
            HttpHeaders.ContentType,
            ContentType.MultiPart.FormData.withParameter("boundary", multipartBoundary).toString()
        )
        return multipartBoundary
    }

    @Test
    fun `Scan using WebSocket scan`() {
        val testFileContent = "Tento jednotkovy test robim pre svetovy mier."
        createTestApplication {
            handleWebSocketConversation(DriverRoutes.scanFileWebSocket) { incoming, outgoing ->
                outgoing.send(
                    Frame.Text(
                        ScanFileWebSocketParameters(
                            useExternalServices = true,
                            originalFilename = testFilename
                        ).json()
                    )
                )

                outgoing.send(
                    Frame.Binary(true, testFileContent.toByteArray())
                )

                val expectedMessageCountForThisTest = 1
                var messageCounter = 0
                for (message in incoming) {
                    if(message !is Frame.Close) {
                        assertTrue(message is Frame.Text)

                        val antivirusReport = message.readText().fromJson<AntivirusReportResponse>()
                        assertEquals(ScanStatus.OK, antivirusReport.status)

                        messageCounter++
                    }
                }
                assertEquals(expectedMessageCountForThisTest, messageCounter)
            }
        }
    }

}
