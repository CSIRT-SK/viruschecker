package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand

internal val defaultPropertiesUnix = """
# Keep records for specified start
${Properties.keepReportsDays}=30

# Comodo
# ==============================================================================

${Properties.Linux.comodo}=/opt/COMODO/cmdscan -s ${RunProgramCommand.SCAN_FILE} -v

# VirusTotal
# ==============================================================================

${Properties.virusTotal}=<insert-your-api-key>
"""