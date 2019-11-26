//package sk.csirt.viruschecker.client.web.routing
//
//import io.ktor.application.call
//import io.ktor.http.HttpStatusCode
//import io.ktor.http.content.PartData
//import io.ktor.http.content.forEachPart
//import io.ktor.http.content.streamProvider
//import io.ktor.locations.KtorExperimentalLocationsAPI
//import io.ktor.locations.post
//import io.ktor.request.receiveMultipart
//import io.ktor.response.respond
//import io.ktor.routing.Route
//import sk.csirt.viruschecker.client.service.GatewayShareService
//import sk.csirt.viruschecker.client.service.ShareParameters
//import sk.csirt.viruschecker.utils.copyToSuspend
//import sk.csirt.viruschecker.utils.tempDirectory
//import java.io.File
//import java.util.*
//
///**
// * Work in progress. Upload file to VirusTotal.
// */
//@KtorExperimentalLocationsAPI
//fun Route.shareFile(shareService: GatewayShareService) {
//
//    post<WebRoutes.ShareFile> {
//        val multipart = call.receiveMultipart()
//        var shareParameters = ShareParameters(
//            file = File(""),
//            originalFilename = ""
//        )
//
//        multipart.forEachPart { part ->
//            when (part) {
//                is PartData.FileItem -> {
//                    val file = File(
//                        tempDirectory,
//                        "${UUID.randomUUID()}-${part.originalFileName}"
//                    )
//
//                    part.streamProvider()
//                        .use { its -> file.outputStream().buffered().use { its.copyToSuspend(it) } }
//                    shareParameters = shareParameters.copy(
//                        file = file,
//                        originalFilename = part.originalFileName ?: file.name
//                    )
//                }
//            }
//            part.dispose()
//        }
//
//        if (shareParameters.file.exists().not()) {
//            call.respond(HttpStatusCode.InternalServerError, "File was not uploaded")
//        } else {
//            val shareResult = shareService.shareFile(shareParameters)
//            call.respond(shareResult)
//        }
//    }
//}