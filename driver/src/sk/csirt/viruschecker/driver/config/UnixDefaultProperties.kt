package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand
import sk.csirt.viruschecker.driver.config.DriverPropertiesFactory.missingApiKeyPlaceHolder

internal const val defaultPropertiesUnix = """
# Comodo
# ==============================================================================

${Properties.comodo}=/opt/COMODO/cmdscan -s ${RunProgramCommand.SCAN_FILE} -v

# VirusTotal
# ==============================================================================

${Properties.virusTotal}=$missingApiKeyPlaceHolder
"""