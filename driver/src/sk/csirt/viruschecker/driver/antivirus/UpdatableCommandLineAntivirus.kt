package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.Constants
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.utils.parseParameter
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.*

abstract class UpdatableCommandLineAntivirus(
        scanCommand: ExecutableCommand,
        private val updateCommand: ExecutableCommand
) : CommandLineAntivirus(scanCommand), UpdatableAntivirus {

    override fun update(): String {
        val reportFile = Paths.get(
            Constants.updateReportsDir,
            "update-${LocalDateTime.now().toString().replace(":", "-")}-${UUID.randomUUID()}.txt"
        ).toFile()
        reportFile.createNewFile()
        val command = buildUpdateCommand(updateCommand, reportFile)
        runAntivirus(command)
        return FileUtils.readFileToString(reportFile, Charset.defaultCharset())
    }

    companion object {
        fun buildUpdateCommand(
                command: ExecutableCommand,
                reportFile: File
        ): List<String> {
            val commandList = mutableListOf(command.executableName, command.flag)
            parseParameter(
                commandList,
                command.reportFlag,
                reportFile.canonicalPath
            )
            command.additionalOptions.takeIf { it.isNotEmpty() }?.also{
                commandList.addAll(it.split(" "))
            }

            return commandList
        }

    }
}