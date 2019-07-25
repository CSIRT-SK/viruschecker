package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand

internal val defaultPropertiesWindows = """
# Keep records for specified start
${Properties.keepReportsDays}=30

# Avast
# ==============================================================================

${Properties.Windows.avast}=ashCmd.exe ${RunProgramCommand.SCAN_FILE} /_> ${RunProgramCommand.REPORT_FILE}

# Eset
# ==============================================================================

${Properties.Windows.eset}=ecls.exe ${RunProgramCommand.SCAN_FILE} /log-file=${RunProgramCommand.REPORT_FILE} /log-all

# Kaspersky
# ==============================================================================

${Properties.Windows.kaspersky}=avp.com scan ${RunProgramCommand.SCAN_FILE} /RA:${RunProgramCommand.REPORT_FILE} /i0

# Microsoft
# ==============================================================================

${Properties.Windows.microsoft}=MpCmdRun.exe -Scan -ScanType 3 -File ${RunProgramCommand.SCAN_FILE} -DisableRemediation

# VirusTotal
# ==============================================================================

${Properties.virusTotal}=<insert-your-api-key>
"""