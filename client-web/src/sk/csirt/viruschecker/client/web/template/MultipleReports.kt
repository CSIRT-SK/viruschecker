package sk.csirt.viruschecker.client.web.template

import io.ktor.application.ApplicationCall
import io.ktor.http.CacheControl
import io.ktor.http.content.Version
import io.ktor.locations.KtorExperimentalLocationsAPI
import kotlinx.html.*
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.ScanStatus
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Function that generates HTML for the structure of the page and allows to provide a [block] that will be placed
 * in the content place of the page.
 */
@KtorExperimentalLocationsAPI
suspend fun ApplicationCall.respondMultipleReportsHtml(
    reports: List<FileHashScanResponse>,
    versions: List<Version> = emptyList(),
    visibility: CacheControl.Visibility = CacheControl.Visibility.Private,
    title: String = "Virus Checker",
    block: DIV.() -> Unit
) {
    respondDefaultHtml {
        block()
        reports.forEach { (sha256, md5, sha1, scanReport) ->
            br()
            strong { +"File ${scanReport.filename}" }
            br()
            +scanReport.date
                .let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }
                .let { "Scan time: ${it.toLocalDate()}, ${it.toLocalTime()}" }

            pStatus(scanReport.status)
            p {
                listOf(
                    "SHA-256" to sha256,
                    "SHA-1" to sha1,
                    "MD5" to md5
                ).forEach { (algorithm, value) ->
                    +"$algorithm: $value"
                    br()
                }
            }
            p {
                +"Antivirus reports:"
                br()
                scanReport.results.forEach { report ->
                    val status = if (report.status >= ScanStatus.NOT_AVAILABLE)
                        report.malwareDescription
                    else
                        report.status.toString()
                    +"${report.antivirus}:${report.virusDatabaseVersion}; $status"
                    br()
                }
            }
            hr(classes = "lhr")
        }
    }
}