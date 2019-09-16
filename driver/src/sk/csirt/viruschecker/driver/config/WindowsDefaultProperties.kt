package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand

internal val defaultPropertiesWindows = """
# Avast
# ==============================================================================

${Properties.avast}=ashCmd.exe ${RunProgramCommand.SCAN_FILE} /_> ${RunProgramCommand.REPORT_FILE}

# Eset
# ==============================================================================

${Properties.eset}=ecls.exe ${RunProgramCommand.SCAN_FILE} /log-all

# Kaspersky
# ==============================================================================

${Properties.kaspersky}=avp.com scan ${RunProgramCommand.SCAN_FILE} /RA /i0

# Microsoft
# ==============================================================================

${Properties.microsoft}=MpCmdRun.exe -Scan -ScanType 3 -File ${RunProgramCommand.SCAN_FILE} -DisableRemediation

# VirusTotal
# ==============================================================================

${Properties.virusTotal}=<insert-your-api-key>
"""

//internal val defaultPropertiesWindows = """
//# Avast
//# ==============================================================================
//
//${Properties.avast}=ashCmd.exe ${RunProgramCommand.SCAN_FILE} /_> ${RunProgramCommand.REPORT_FILE}
//
//# Eset
//# ==============================================================================
//
//${Properties.eset}=ecls.exe ${RunProgramCommand.SCAN_FILE} /log-file=${RunProgramCommand.REPORT_FILE} /log-all
//
//# Kaspersky
//# ==============================================================================
//
//${Properties.kaspersky}=avp.com scan ${RunProgramCommand.SCAN_FILE} /RA:${RunProgramCommand.REPORT_FILE} /i0
//
//# Microsoft
//# ==============================================================================
//
//${Properties.microsoft}=MpCmdRun.exe -Scan -ScanType 3 -File ${RunProgramCommand.SCAN_FILE} -DisableRemediation
//
//# VirusTotal
//# ==============================================================================
//
//${Properties.virusTotal}=<insert-your-api-key>
//"""