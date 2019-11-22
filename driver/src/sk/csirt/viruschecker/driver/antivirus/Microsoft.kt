package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.apache.commons.lang3.SystemUtils
import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.utils.ProcessRunner
import sk.csirt.viruschecker.driver.utils.WindowsRegistry

class Microsoft(
    scanCommand: RunProgramCommand,
    processRunner: ProcessRunner,
    private val registry: WindowsRegistry
) : CommandLineAntivirus(
    scanCommand,
    processRunner
) {

    private val logger = KotlinLogging.logger { }

    override val antivirusName: String = AntivirusType.MICROSOFT.antivirusName

    override suspend fun parseReport(
        rawReport: List<String>,
        params: FileScanParameters
    ): Report = coroutineScope {
        val virusDatabaseVersion = let {
            val registryKey = if (SystemUtils.IS_OS_WINDOWS_7)
                "HKLM\\SOFTWARE\\Microsoft\\Microsoft Antimalware\\Signature Updates"
            else
                "HKLM\\SOFTWARE\\Microsoft\\Windows Defender\\Signature Updates"

            async {
                registry.read(
                    registryKey,
                    "AVSignatureVersion"
                )
            }
        }

        val infectedCountString = rawReport.firstOrNull { line ->
            line.startsWith("Scanning ")
                    && "found" in line
                    && line.endsWith(" threats.")
        }?.split(" ")?.let { it[it.size - 2] }
        logger.debug { "Found $infectedCountString threats." }
        val infectedCount = infectedCountString?.toIntOrNull() ?: 0

        // return from coroutine
        when {
            infectedCountString == null -> Report(ScanStatusResult.NOT_AVAILABLE, "", "")
            infectedCount == 0 -> Report(ScanStatusResult.OK, "OK", virusDatabaseVersion.await())
            infectedCount >= 1 -> {
                val description = rawReport
                    .first { it.startsWith("Threat") }
                    .split(": ")[1]
                Report(
                    ScanStatusResult.INFECTED,
                    description,
                    virusDatabaseVersion.await()
                )
            }
            else -> Report(ScanStatusResult.NOT_AVAILABLE, "", "")
        }
    }
}