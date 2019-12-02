package sk.csirt.viruschecker.gateway.routing.utils

import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import io.ktor.util.KtorExperimentalAPI
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*

@KtorExperimentalAPI
internal fun PartData.FileItem.toTempFile(): File {
    val tempFile = createTempFile()
    FileUtils.copyInputStreamToFile(streamProvider(), tempFile)
    return tempFile
}

