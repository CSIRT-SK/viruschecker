package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.ScanStatus

class CommandLineReporter : Reporter {

    override fun saveReport(scanResponse: FileHashScanResponse) {

        val headerMessage = "Scan report for file ${scanResponse.report.filename}"
        val headerDelimiter = "-".repeat(80)

        """
$headerDelimiter
$headerMessage
$headerDelimiter
Scan scanResponse: ${scanResponse.report.status}

SHA-256: ${scanResponse.sha256}
  SHA-1: ${scanResponse.sha1}
    MD5: ${scanResponse.md5}
    
Antivirus reports:
${scanResponse.report.results.joinToString("\n") {
            if(it.status==ScanStatus.INFECTED)
                "${it.antivirus}: ${it.status} (${it.malwareDescription}), " +
                        "db_version: ${it.virusDatabaseVersion}"
            else
                "${it.antivirus}: ${it.status}, db_version: ${it.virusDatabaseVersion}"    
            
            
        }
        }
        """.trimIndent()
            .also {
                println(it)
            }
    }
}
