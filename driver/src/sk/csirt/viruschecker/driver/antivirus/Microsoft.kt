package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.utils.readHKLMRegistryKey

class Microsoft(
    scanCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    private val logger = KotlinLogging.logger { }

    override val antivirusName: String = AntivirusType.MICROSOFT.antivirusName

    override suspend fun parseReport(
        report: List<String>,
        params: FileScanParameters
    ): Report = coroutineScope {
        val virusDatabaseVersionDeferred = async(Dispatchers.IO) {
            readHKLMRegistryKey(
                "HKLM\\SOFTWARE\\Microsoft\\Microsoft Antimalware\\Signature Updates",
                "AVSignatureVersion"
            )
        }

        val infectedCountString = report.firstOrNull {
            it.startsWith("Scanning ") &&
                    "found" in it &&
                    it.endsWith(" threats.")
        }?.split(" ")?.let { it[it.size - 2] }
        logger.debug { "Found $infectedCountString threats." }
        val infectedCount = infectedCountString?.toIntOrNull()

        val virusDabaseVersion = virusDatabaseVersionDeferred.await()

        // return from coroutine
        when {
            infectedCountString == null -> Report(ScanStatusResult.NOT_AVAILABLE, "", "")
            infectedCountString == "no" -> Report(ScanStatusResult.OK, "OK", virusDabaseVersion)
            infectedCount != null -> {
                val description = report.first { it.startsWith("Threat") }.split(": ")[1]
                Report(ScanStatusResult.INFECTED, description, virusDabaseVersion)
            }
            else -> Report(ScanStatusResult.NOT_AVAILABLE, "", "")
        }
    }
}