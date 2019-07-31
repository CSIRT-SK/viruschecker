package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand

internal val defaultPropertiesUnix = """
# Comodo
# ==============================================================================

${Properties.comodo}=/opt/COMODO/cmdscan -s ${RunProgramCommand.SCAN_FILE} -v

# VirusTotal
# ==============================================================================

${Properties.virusTotal}=<insert-your-api-key>
"""