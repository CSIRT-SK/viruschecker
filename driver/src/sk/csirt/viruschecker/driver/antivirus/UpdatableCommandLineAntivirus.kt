package sk.csirt.viruschecker.driver.antivirus

abstract class UpdatableCommandLineAntivirus(
    scanCommand: RunProgramCommand,
    private val updateCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand), UpdatableAntivirus {

    override suspend fun update(): String {
        logger.info("Updating antivirusName")
        val output = runAntivirus(updateCommand.parse())
        logger.info { "Update report is: $output" }
        return output.joinToString("; ")
    }
}