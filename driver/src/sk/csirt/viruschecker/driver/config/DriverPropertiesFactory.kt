package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.config.PropertiesFactory
import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand.Placeholder.REPORT_FILE
import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand.Placeholder.SCAN_FILE
import sk.csirt.viruschecker.driver.config.Properties.Linux.comodo
import sk.csirt.viruschecker.driver.config.Properties.Windows.avast
import sk.csirt.viruschecker.driver.config.Properties.Windows.eset
import sk.csirt.viruschecker.driver.config.Properties.Windows.kaspersky
import sk.csirt.viruschecker.driver.config.Properties.Windows.microsoft
import sk.csirt.viruschecker.driver.config.Properties.keepReportsDays
import sk.csirt.viruschecker.driver.config.Properties.scanTimeout
import sk.csirt.viruschecker.driver.config.Properties.virusTotal

object Properties {

    const val keepReportsDays = "keep.results.days"
    const val scanTimeout = "scan.socketTimeout.millis"

    object Windows{
        const val avast = "avast.windows"
        const val eset = "eset.windows"
        const val kaspersky = "kaspersky.windows"
        const val microsoft = "microsoft.windows"
    }

    object Linux{
        const val comodo = "comodo.linux"
    }

    const val virusTotal = "virustotal.apikey"
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
