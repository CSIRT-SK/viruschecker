package sk.csirt.viruschecker.driver.antivirus

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.driver.utils.ProcessRunner
import sk.csirt.viruschecker.driver.utils.WindowsRegistry

@ExperimentalCoroutinesApi
internal class MicrosoftTest : CommandLineAntivirusTest() {
    override fun mockFileScanOutputHealthy(filename: String) =
        """Scan starting...
Scan finished.
Scanning $filename found no threats.
""".trimIndent()

    override fun mockFileScanOutputInfected(filename: String) =
        """Scan starting...
Scan finished.
Scanning c:\inf.txt found 1 threats.

<===========================LIST OF DETECTED THREATS==========================>
----------------------------- Threat information ------------------------------
Threat                  : Virus:DOS/EICAR_Test_File
Resources               : 1 total
    file                : $filename
-------------------------------------------------------------------------------
""".trimIndent()

    override fun mockArchiveFileScanOutputHealthy(filename: String) =
        """Scan starting...
Scan finished.
Scanning $filename found no threats.
""".trimIndent()

    override fun mockArchiveFileScanOutputInfected(filename: String) =
        """Scan starting...
Scan finished.
Scanning c:\inf.zip found 1 threats.

<===========================LIST OF DETECTED THREATS==========================>
----------------------------- Threat information ------------------------------
Threat                  : Virus:DOS/EICAR_Test_File
Resources               : 2 total
    file                : $filename->eicar.exe
    containerfile       : $filename
-------------------------------------------------------------------------------
""".trimIndent()

    override fun antivirusFactory(
        command: RunProgramCommand,
        processRunner: ProcessRunner
    ): CommandLineAntivirus {
        val registry = mockk<WindowsRegistry>(){
            coEvery { read(any(), any()) } returns "1.1.1701"
        }
        return Microsoft(command, processRunner, registry)
    }
}