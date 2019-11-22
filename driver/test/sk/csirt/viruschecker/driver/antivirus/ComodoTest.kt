package sk.csirt.viruschecker.driver.antivirus

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.driver.utils.ProcessRunner
import sk.csirt.viruschecker.driver.utils.WindowsRegistry

@ExperimentalCoroutinesApi
internal class ComodoTest : CommandLineAntivirusTest() {
    override fun mockFileScanOutputHealthy(filename: String) =
        """-----== Scan Start ==-----
$filename ---> Not Virus
-----== Scan End ==-----
Number of Scanned Files: 1
Number of Found Viruses: 0
""".trimIndent()

    override fun mockFileScanOutputInfected(filename: String) =
        """-----== Scan Start ==-----
$filename ---> Found Virus, Malware Name is ApplicUnwnt
-----== Scan End ==-----
Number of Scanned Files: 1
Number of Found Viruses: 1
""".trimIndent()

    override fun mockArchiveFileScanOutputHealthy(filename: String) =
        """-----== Scan Start ==-----
$filename ---> Not Virus
-----== Scan End ==-----
Number of Scanned Files: 1
Number of Found Viruses: 0
""".trimIndent()

    override fun mockArchiveFileScanOutputInfected(filename: String) =
        """-----== Scan Start ==-----
$filename ---> Found Virus, Malware Name is ApplicUnwnt
-----== Scan End ==-----
Number of Scanned Files: 1
Number of Found Viruses: 1
""".trimIndent()

    override fun antivirusFactory(
        command: RunProgramCommand,
        processRunner: ProcessRunner
    ): CommandLineAntivirus =
        Comodo(command, processRunner)
}