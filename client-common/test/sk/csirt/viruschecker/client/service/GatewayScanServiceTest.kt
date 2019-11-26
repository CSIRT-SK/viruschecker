package sk.csirt.viruschecker.client.service

import io.ktor.client.engine.mock.respond
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import sk.csirt.viruschecker.client.config.jsonHeaders
import sk.csirt.viruschecker.client.config.mockHttpClient
import sk.csirt.viruschecker.client.testScanResponse
import sk.csirt.viruschecker.routing.GatewayRoutes
import sk.csirt.viruschecker.utils.json
import kotlin.test.Test
import kotlin.test.assertEquals

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class GatewayScanServiceTest {

    private val testResponse get() = testScanResponse

    private val mockHttpClient = mockHttpClient { request ->
        when (request.method) {
            HttpMethod.Post -> when (request.url.encodedPath) {
                GatewayRoutes.multiScanFile -> {
                    if (request.body !is MultiPartFormDataContent) error(
                        "Endpoint {$GatewayRoutes.multiScanFile} expects multipart/form-data body"
                    )
                    respond(
                        content = testScanResponse.json(),
                        headers = jsonHeaders
                    )
                }
                else -> error("Unhandled request ${request.url.encodedPath}")
            }

//            HttpMethod.Get -> when(request.url.encodedPath){
//                GatewayRoutes.multiScanFileWebSocket -> {
//                    respond(
//                        content = testScanResponse.json(),
//                        headers = jsonHeaders
//                    )
//                }
//                else -> error("Unhandled request ${request.url.encodedPath}")
//            }

            else -> error("Unsupported HTTP method")
        }

    }

    private val gatewayScanService = GatewayScanService("http://localhost:8080", mockHttpClient)


    @Test
    fun `Scan file test`() = runBlockingTest {
        val file = createTempFile()
        val testScanParameters = ScanParameters(
            fileToScan = file,
            originalFilename = "A-ja-taka-dzivocka",
            useExternalDrivers = true
        )

        val response = gatewayScanService.scanFile(testScanParameters)
        assertEquals(testResponse, response)
    }

//    @Test
//    fun `Scan file using WebSocket test`() = runBlockingTest {
//        val file = createTempFile()
//        val testScanParameters = ScanParameters(
//            fileToScan = file,
//            originalFilename = "A-ja-taka-dzivocka",
//            useExternalDrivers = true
//        )
//
//        gatewayScanService.scanFileWebSocket(testScanParameters,
//            { }
//        ) {
//
//        }
//
//    }
}