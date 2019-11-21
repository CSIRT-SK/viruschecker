package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.driver.utils.ProcessRunner

@ExperimentalCoroutinesApi
internal class EsetTest : CommandLineAntivirusTest() {
    override fun mockFileScanOutputHealthy(filename: String) =
        """ECLS Command-line scanner, version 12.2.29.0, (C) 1992-2019 ESET, spol. s r.o.
Module loader, version 1018.1 (20190709), build 1054
Module perseus, version 1555 (20190911), build 2060
Module scanner, version 20102 (20190930), build 42937
Module archiver, version 1292 (20190911), build 1307
Module advheur, version 1193 (20190626), build 1175
Module cleaner, version 1200 (20190916), build 1303

Command line: $filename /log-all

Scan started at:   Wed Nov 20 13:41:54 2019
name="$filename", result="is OK", action="", info=""

Scan completed at: Wed Nov 20 13:41:54 2019
Scan time:         0 sec (0:00:00)
Total:             files - 1, objects 1
Detected:          files - 0, objects 0
Cleaned:           files - 0, objects 0
""".trimIndent()

    override fun mockFileScanOutputInfected(filename: String) =
        """ECLS Command-line scanner, version 12.2.29.0, (C) 1992-2019 ESET, spol. s r.o.
Module loader, version 1018.1 (20190709), build 1054
Module perseus, version 1555 (20190911), build 2060
Module scanner, version 20102 (20190930), build 42937
Module archiver, version 1292 (20190911), build 1307
Module advheur, version 1193 (20190626), build 1175
Module cleaner, version 1200 (20190916), build 1303

Command line: $filename /log-all

Scan started at:   Wed Nov 20 13:47:52 2019
name="$filename", result="Eicar test file", action="", info=""

Scan completed at: Wed Nov 20 13:47:52 2019
Scan time:         0 sec (0:00:00)
Total:             files - 1, objects 1
Detected:          files - 1, objects 1
Cleaned:           files - 0, objects 0
""".trimIndent()

    override fun mockArchiveFileScanOutputHealthy(filename: String) =
        """ECLS Command-line scanner, version 12.2.29.0, (C) 1992-2019 ESET, spol. s r.o.
Module loader, version 1018.1 (20190709), build 1054
Module perseus, version 1555 (20190911), build 2060
Module scanner, version 20102 (20190930), build 42937
Module archiver, version 1292 (20190911), build 1307
Module advheur, version 1193 (20190626), build 1175
Module cleaner, version 1200 (20190916), build 1303

Command line: "$filename" /log-all

Scan started at:   Wed Nov 20 13:50:45 2019
name="$filename", result="is OK", action="", info=""
name="$filename » ZIP » go/bin/rsrc.exe", result="is OK", action="", info=""
name="$filename » ZIP » go/pkg/windows_amd64/github.com/kardianos/osext.a", result="is OK", action="", info=""

Scan completed at: Wed Nov 20 13:50:45 2019
Scan time:         0 sec (0:00:00)
Total:             files - 1, objects 3
Detected:          files - 0, objects 0
Cleaned:           files - 0, objects 0
C:\csd>
""".trimIndent()

    override fun mockArchiveFileScanOutputInfected(filename: String) =
        """ECLS Command-line scanner, version 12.2.29.0, (C) 1992-2019 ESET, spol. s r.o.
Module loader, version 1018.1 (20190709), build 1054
Module perseus, version 1555 (20190911), build 2060
Module scanner, version 20102 (20190930), build 42937
Module archiver, version 1292 (20190911), build 1307
Module advheur, version 1193 (20190626), build 1175
Module cleaner, version 1200 (20190916), build 1303

Command line: $filename /log-all

Scan started at:   Wed Nov 20 13:54:42 2019
name="$filename", result="Eicar test file", action="", info=""
name="$filename » ZIP » go/bin/rsrc.exe", result="is OK", action="", info=""
name="$filename » ZIP » go/pkg/windows_amd64/github.com/kardianos/osext.a", result="is OK", action="", info=""
name="inf.zip » ZIP » eicar.exe", result="Eicar test file", action="", info=""

Scan completed at: Wed Nov 20 13:54:42 2019
Scan time:         0 sec (0:00:00)
Total:             files - 1, objects 4
Detected:          files - 1, objects 1
Cleaned:           files - 0, objects 0
""".trimIndent()

    override fun antivirusFactory(
        command: RunProgramCommand,
        processRunner: ProcessRunner
    ): CommandLineAntivirus =
        Eset(command, processRunner)
}