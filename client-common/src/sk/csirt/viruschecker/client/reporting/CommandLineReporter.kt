package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.ScanStatusResponse

class CommandLineReporter : Reporter {

    override fun saveReport(result: FileHashScanResponse) {

        val headerMessage = "Scan report for file ${result.report.filename}"
        val headerDelimiter = "-".repeat(80)

        """
$headerDelimiter
$headerMessage
$headerDelimiter
Scan result: ${result.report.status}

SHA-256: ${result.sha256}
  SHA-1: ${result.sha1}
    MD5: ${result.md5}
    
Antivirus reports:
${result.report.results.joinToString("\n") {
            if(it.status==ScanStatusResponse.INFECTED)
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
