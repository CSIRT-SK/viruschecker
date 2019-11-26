//package sk.csirt.viruschecker.client.service
//
//import io.ktor.client.HttpClient
//import io.ktor.client.request.forms.MultiPartFormDataContent
//import io.ktor.client.request.post
//import io.ktor.http.ContentDisposition
//import io.ktor.http.Headers
//import io.ktor.http.HttpHeaders
//import io.ktor.http.content.PartData
//import kotlinx.io.streams.asInput
//import sk.csirt.viruschecker.routing.GatewayRoutes
//import java.io.File
//import java.io.FileInputStream
//
//data class ShareParameters(
//    val file: File,
//    val originalFilename: String
//)
//
//class GatewayShareService(
//    private val gatewayUrl: String,
//    private val client: HttpClient
//) {
//    suspend fun shareFile(params: ShareParameters): String =
//        client.post("$gatewayUrl${GatewayRoutes.shareFile}") {
//            this.body = MultiPartFormDataContent(
//                listOf(
//                    PartData.FileItem(
//                        partHeaders = Headers.build {
//                            this[HttpHeaders.ContentDisposition] =
//                                ContentDisposition.File.withParameter(
//                                    "filename",
//                                    params.originalFilename
//                                ).toString()
//                        },
//                        dispose = { },
//                        provider = { FileInputStream(params.file).asInput() }
//                    )
//                )
//            )
//        }
//}
//
//
