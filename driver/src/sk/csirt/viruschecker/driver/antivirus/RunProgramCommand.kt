package sk.csirt.viruschecker.driver.antivirus

import java.io.File

data class RunProgramCommand(
    val command: String
) {
    fun parse(
        fileToScan: File? = null,
        fileToReport: File? = null
    ): List<String> {
        val splittedCommand = if (command.startsWith("\"")) {
            val doubleQuotesEnd = command.indexOf("\"", 2)
            listOf(
                command.substring(
                    1,
                    doubleQuotesEnd
                )
            ) + command.substring(doubleQuotesEnd + 1)
                .split(" ")
        } else {
            command.split(" ")
        }

        return splittedCommand.map { word ->
            when {
                SCAN_FILE in word ->
                    word.replace(SCAN_FILE, fileToScan?.canonicalPath ?: "")
                REPORT_FILE in word ->
                    word.replace(REPORT_FILE, fileToReport?.canonicalPath ?: "")
                else -> word
            }
        }
    }

    companion object Placeholder {
        const val SCAN_FILE = "[SCAN-FILE]"
        const val REPORT_FILE = "[REPORT-FILE]"
    }
}