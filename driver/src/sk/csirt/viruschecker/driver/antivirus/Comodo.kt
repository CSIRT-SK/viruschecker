package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.driver.config.AntivirusType
import sk.csirt.viruschecker.driver.utils.ProcessRunner

@ExperimentalCoroutinesApi
class Comodo(
    scanCommand: RunProgramCommand,
    processRunner: ProcessRunner
) : CommandLineAntivirus(
    scanCommand,
    processRunner
) {

    override val antivirusName: String = AntivirusType.COMODO.antivirusName

    override suspend fun parseReport(
        rawReport: List<String>,
        params: FileScanParameters
    ): Report =
        rawReport[1]
            .split("--->")[1]
            .trim()
            .let {
                when {
                    it == "Not Virus" ->
                        Report(ScanStatusResult.OK, it, "Not available ATM")
                    it.startsWith("Found Virus") ->
                        Report(ScanStatusResult.INFECTED, it.split(" is ")[1], "Not available ATM")
                    else -> Report(ScanStatusResult.NOT_AVAILABLE, "", "Not available ATM")
                }
            }
}
