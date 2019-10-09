package sk.csirt.viruschecker.driver.antivirus

import java.io.File

data class RunProgramCommand(
    val command: String
) {

    fun parse(
        fileToScan: File? = null,
        fileToReport: File? = null
    ): List<String> = command.split(" ").map {
        when {
            SCAN_FILE in it ->
                it.replace(SCAN_FILE, fileToScan?.canonicalPath ?: "")
            REPORT_FILE in it ->
                it.replace(REPORT_FILE, fileToReport?.canonicalPath ?: "")
            else -> it
        }
    }

    companion object Placeholder {
        const val SCAN_FILE = "[SCAN-FILE]"
        const val REPORT_FILE = "[REPORT-FILE]"
    }
}