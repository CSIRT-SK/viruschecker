package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.driver.utils.ProcessRunner

@ExperimentalCoroutinesApi
internal class KasperskyTest : CommandLineAntivirusTest() {
    override fun mockFileScanOutputHealthy(filename: String) =
        """AV bases release date: 2019-11-21 11:49:00 (full)
; --- Settings ---
; Action on detect:     Report only
; Scan objects: All infectable
; Use iChecker: Yes
; Use iSwift:   Yes
; Try disinfect:        No
; Try delete:   No
; Try delete container: No
; Time limit:   180 sec.
; Exclude by mask:      No
; Include by mask:      No
; Objects to scan:
;       "$filename" Enable = Yes    Recursive = No
; ------------------
2019-11-21 14:53:54     Scan_Objects${'$'}3941                          starting   1%

2019-11-21 14:53:54     $filename   ok
2019-11-21 14:53:54     Scan_Objects${'$'}3941                          running    10
0%
2019-11-21 14:53:54     Scan_Objects${'$'}3941                          completed

Info: task 'ods' finished, last error code 0
;  --- Statistics ---
; Time Start:   2019-11-21 14:53:54
; Time Finish:  2019-11-21 14:53:54
; Processed objects:    1
; Total OK:     1
; Total detected:       0
; Suspicions:   0
; Total skipped:        0
; Password protected:   0
; Corrupted:    0
; Errors:       0
;  ------------------
""".trimIndent()

    override fun mockFileScanOutputInfected(filename: String) =
        """AV bases release date: 2019-11-21 06:11:00 (full)
; --- Settings ---
; Action on detect:     Report only
; Scan objects: All infectable
; Use iChecker: Yes
; Use iSwift:   Yes
; Try disinfect:        No
; Try delete:   No
; Try delete container: No
; Time limit:   180 sec.
; Exclude by mask:      No
; Include by mask:      No
; Objects to scan:
;       "$filename"        Enable = Yes    Recursive = No
; ------------------
2019-11-21 11:06:55     Scan_Objects${'$'}3923                          starting   1%

2019-11-21 11:06:55     Scan_Objects${'$'}3923                          running    1%

2019-11-21 11:06:56     $filename  detected        EICAR-Test-File
2019-11-21 11:06:56     $filename  skipped
2019-11-21 11:06:56     Scan_Objects${'$'}3923                          completed

Info: task 'ods' finished, last error code 0
;  --- Statistics ---
; Time Start:   2019-11-21 11:06:55
; Time Finish:  2019-11-21 11:06:56
; Processed objects:    1
; Total OK:     0
; Total detected:       1
; Suspicions:   0
; Total skipped:        0
; Password protected:   0
; Corrupted:    0
; Errors:       0
;  ------------------
""".trimIndent()

    override fun mockArchiveFileScanOutputHealthy(filename: String) =
        """AV bases release date: 2019-11-21 06:11:00 (full)
; --- Settings ---
; Action on detect:     Report only
; Scan objects: All infectable
; Use iChecker: Yes
; Use iSwift:   Yes
; Try disinfect:        No
; Try delete:   No
; Try delete container: No
; Time limit:   180 sec.
; Exclude by mask:      No
; Include by mask:      No
; Objects to scan:
;       "$filename" Enable = Yes    Recursive = No
; ------------------
2019-11-21 11:09:54     Scan_Objects${'$'}3929                          starting   1%

2019-11-21 11:09:54     Scan_Objects${'$'}3929                          running    1%

2019-11-21 11:09:54     $filename   archive ZIP
2019-11-21 11:09:54     $filename//go/bin/rsrc.exe  ok
2019-11-21 11:09:54     $filename//go/pkg/windows_amd64/github.com/kardianos/osext.a        ok
2019-11-21 11:09:54    $filename   skipped: by type
2019-11-21 11:09:54     Scan_Objects${'$'}3929                          completed

Info: task 'ods' finished, last error code 0
;  --- Statistics ---
; Time Start:   2019-11-21 11:09:54
; Time Finish:  2019-11-21 11:09:54
; Processed objects:    4
; Total OK:     4
; Total detected:       0
; Suspicions:   0
; Total skipped:        0
; Password protected:   0
; Corrupted:    0
; Errors:       0
;  ------------------
""".trimIndent()

    override fun mockArchiveFileScanOutputInfected(filename: String) =
        """AV bases release date: 2019-11-21 06:11:00 (full)
; --- Settings ---
; Action on detect:     Report only
; Scan objects: All infectable
; Use iChecker: Yes
; Use iSwift:   Yes
; Try disinfect:        No
; Try delete:   No
; Try delete container: No
; Time limit:   180 sec.
; Exclude by mask:      No
; Include by mask:      No
; Objects to scan:
;       "$filename"        Enable = Yes    Recursive = No
; ------------------
2019-11-21 11:08:55     Scan_Objects${'$'}3927                          starting   1%

2019-11-21 11:08:55     Scan_Objects${'$'}3927                          running    1%

2019-11-21 11:08:55     $filename  archive ZIP
2019-11-21 11:08:56     $filename//go/bin/rsrc.exe ok
2019-11-21 11:08:56     $filename//go/pkg/windows_amd64/github.com/kardianos/osext.a       archive arch
2019-11-21 11:08:56     $filename//go/pkg/windows_amd64/github.com/kardianos/osext.a       skipped: by type
2019-11-21 11:08:56     $filename//eicar.exe       detected        EICAR-Test-File
2019-11-21 11:08:56     $filename//eicar.exe       skipped
2019-11-21 11:08:56     Scan_Objects${'$'}3927                          completed

Info: task 'ods' finished, last error code 0
;  --- Statistics ---
; Time Start:   2019-11-21 11:08:55
; Time Finish:  2019-11-21 11:08:56
; Processed objects:    4
; Total OK:     3
; Total detected:       1
; Suspicions:   0
; Total skipped:        0
; Password protected:   0
; Corrupted:    0
; Errors:       0
;  ------------------
""".trimIndent()

    override fun antivirusFactory(
        command: RunProgramCommand,
        processRunner: ProcessRunner
    ): CommandLineAntivirus =
        Kaspersky(command, processRunner)
}