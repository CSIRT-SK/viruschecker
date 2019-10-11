package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType

class Comodo(scanCommand: RunProgramCommand) : CommandLineAntivirus(scanCommand) {

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
                        Report(ScanStatusResult.OK, it, "")
                    it.startsWith("Found Virus") ->
                        Report(ScanStatusResult.INFECTED, it.split(" is ")[1], "")
                    else -> Report(ScanStatusResult.NOT_AVAILABLE, "", "Not available ATM")
                }
            }
}
