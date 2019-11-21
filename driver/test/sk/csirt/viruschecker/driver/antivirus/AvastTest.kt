package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.driver.utils.ProcessRunner

@ExperimentalCoroutinesApi
internal class AvastTest : CommandLineAntivirusTest() {
    override fun mockFileScanOutputHealthy(filename: String) =
        """$filename	OK
# ----------------------------------------------------------------
# Number of scanned files: 1
# Number of scanned folders: 0
# Number of infected files: 0
# Total size of scanned files: 133
# Virus database: 191119-0, 11/19/19
# Total scan time: 0:0:4
""".trimIndent()

    override fun mockFileScanOutputInfected(filename: String) =
        """$filename	EICAR Test-NOT virus!!!
# ----------------------------------------------------------------
# Number of scanned files: 1
# Number of scanned folders: 0
# Number of infected files: 1
# Total size of scanned files: 68
# Virus database: 191119-0, 11/19/19
# Total scan time: 0:0:3

""".trimIndent()

    override fun mockArchiveFileScanOutputHealthy(filename: String) =
        """$filename	OK
# ----------------------------------------------------------------
# Number of scanned files: 1
# Number of scanned folders: 0
# Number of infected files: 0
# Total size of scanned files: 1528649
# Virus database: 191119-0, 11/19/19
# Total scan time: 0:0:3

""".trimIndent()

    // Avast cannot properly scan infected archives, always returning OK in real scans
    override fun mockArchiveFileScanOutputInfected(filename: String) =
        """$filename	EICAR Test-NOT virus!!!
# ----------------------------------------------------------------
# Number of scanned files: 1
# Number of scanned folders: 0
# Number of infected files: 0
# Total size of scanned files: 1528863
# Virus database: 191119-0, 11/19/19
# Total scan time: 0:0:3

""".trimIndent()

    override fun antivirusFactory(
        command: RunProgramCommand,
        processRunner: ProcessRunner
    ): CommandLineAntivirus =
        Avast(command, processRunner)

}