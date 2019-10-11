package sk.csirt.viruschecker.driver.antivirus

import java.io.File

data class RunProgramCommand(
    val command: String
) {

    fun parse(
        fileToScan: File? = null,
        fileToReport: File? = null
    ): List<String> = command.split(" ").map { word ->
        when {
            SCAN_FILE in word ->
                word.replace(SCAN_FILE, fileToScan?.canonicalPath ?: "")
            REPORT_FILE in word ->
                word.replace(REPORT_FILE, fileToReport?.canonicalPath ?: "")
            else -> word
        }
    }

    companion object Placeholder {
        const val SCAN_FILE = "[SCAN-FILE]"
        const val REPORT_FILE = "[REPORT-FILE]"
    }
}