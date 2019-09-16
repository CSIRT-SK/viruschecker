package sk.csirt.viruschecker.gateway.routing

import io.ktor.http.content.PartData
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.asStream
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Paths
import java.util.*

@KtorExperimentalAPI
internal fun PartData.FileItem.toTempFile(): File {
    val filename = originalFileName ?: "file${UUID.randomUUID()}"
    val tempFileName = "${UUID.randomUUID()}_$filename"
    val tempFile = Paths.get(System.getProperty("java.io.tmpdir"), tempFileName).toFile()
    FileUtils.copyInputStreamToFile(provider().asStream(), tempFile)
    return tempFile
}