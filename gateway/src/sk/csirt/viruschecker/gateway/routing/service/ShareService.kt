//package sk.csirt.viruschecker.gateway.routing.service
//
//import com.kanishka.virustotalv2.VirustotalPublicV2Impl
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.launch
//import mu.KotlinLogging
//import java.io.File
//
//inline class ShareParameters(
//    val file: File
//)
//
//class ShareService {
//
//    private val logger = KotlinLogging.logger{}
//
//    suspend fun shareFile(shareParameters: ShareParameters) = coroutineScope {
//        val virusTotalRef = VirustotalPublicV2Impl()
//        logger.info { "Sending file ${shareParameters.file} to VirusTotal" }
//        launch {
//            runCatching {
//                virusTotalRef.scanFile(shareParameters.file)
//            }.onSuccess {
//                logger.info {
//                    "File ${shareParameters.file} shared successfully with VirusTotal."
//                }
//            }.onFailure {
//                logger.warn {
//                    "File ${shareParameters.file} was not shared with VirusTotal due to the " +
//                            "following reason: $it."
//                }
//            }
//        }
//        Unit
//    }
//}
