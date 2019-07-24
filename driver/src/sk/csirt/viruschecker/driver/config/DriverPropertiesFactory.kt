package sk.csirt.viruschecker.driver.config

import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.config.PropertiesFactory
import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand.Placeholder.REPORT_FILE
import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand.Placeholder.SCAN_FILE
import sk.csirt.viruschecker.driver.config.Properties.Windows.avast
import sk.csirt.viruschecker.driver.config.Properties.Linux.comodo
import sk.csirt.viruschecker.driver.config.Properties.Windows.eset
import sk.csirt.viruschecker.driver.config.Properties.Windows.kaspersky
import sk.csirt.viruschecker.driver.config.Properties.keepReportsDays
import sk.csirt.viruschecker.driver.config.Properties.Windows.microsoft
import sk.csirt.viruschecker.driver.config.Properties.scanTimeout
import sk.csirt.viruschecker.driver.config.Properties.virusTotal

object Properties {

    val keepReportsDays = "keep.results.days"
    val scanTimeout = "scan.socketTimeout.millis"

    object Windows{
        val avast = "avast.windows"
        val eset = "eset.windows"
        val kaspersky = "kaspersky.windows"
        val microsoft = "microsoft.windows"
    }

    object Linux{
        val comodo = "comodo.linux"
    }

    val virusTotal = "virustotal.apikey"
}

object DriverPropertiesFactory : PropertiesFactory {

    override val propertiesName = "viruschecker-driver.properties"

    override val defaultContent = """
# Keep records for specified start
$keepReportsDays=30
# Scan socketTimeout
$scanTimeout=15000

# Avast
# ==============================================================================

$avast=ashCmd.exe $SCAN_FILE /_> $REPORT_FILE

# Eset
# ==============================================================================

$eset=ecls.exe $SCAN_FILE /log-file=$REPORT_FILE /log-all

# Kaspersky
# ==============================================================================

$kaspersky=avp.com scan $SCAN_FILE /RA:$REPORT_FILE /i0

# Microsoft
# ==============================================================================

$microsoft=MpCmdRun.exe -Scan -ScanType 3 -File $SCAN_FILE -DisableRemediation

# Comodo
# ==============================================================================

$comodo=/opt/COMODO/cmdscan -s $SCAN_FILE -v

# VirusTotal
# ==============================================================================

$virusTotal=<insert-your-api-key>
"""
}
