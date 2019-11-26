//package sk.csirt.viruschecker.gateway.routing
//
//
//import io.ktor.application.call
//import io.ktor.http.HttpStatusCode
//import io.ktor.http.content.PartData
//import io.ktor.http.content.forEachPart
//import io.ktor.locations.KtorExperimentalLocationsAPI
//import io.ktor.locations.post
//import io.ktor.request.receiveMultipart
//import io.ktor.response.respond
//import io.ktor.routing.Route
//import io.ktor.util.KtorExperimentalAPI
//import mu.KotlinLogging
//import sk.csirt.viruschecker.gateway.routing.service.ShareParameters
//import sk.csirt.viruschecker.gateway.routing.service.ShareService
//import sk.csirt.viruschecker.gateway.routing.utils.toTempFile
//import sk.csirt.viruschecker.routing.GatewayRoutes
//import java.io.File
//
//private val logger = KotlinLogging.logger { }
//
//@KtorExperimentalAPI
//@KtorExperimentalLocationsAPI
//fun Route.shareFile(shareService: ShareService) {
//    post<GatewayRoutes.ShareFile> {
//        val multipart = call.receiveMultipart()
//        logger.info("Receiving file")
//
//        var fileToScan: File? = null
//
//        multipart.forEachPart { part ->
//            when (part) {
//                is PartData.FileItem -> {
//                    fileToScan = part.toTempFile()
//                }
//            }
//            part.dispose()
//        }
//
//
//        logger.info {
//            "Received request to share file $fileToScan with external services."
//        }
//
//        if (fileToScan == null) {
//            call.respond(HttpStatusCode.BadRequest, "File was not received.")
//        }
//
//        shareService.shareFile(
//            ShareParameters(
//                file = fileToScan!!
//            )
//        )
//
//        call.respond(
//            "File was shared."
//        )
//    }
//}
//
