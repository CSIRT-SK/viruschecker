package sk.csirt.viruschecker.driver.config

import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.antivirus.ScanCommand
import sk.csirt.viruschecker.driver.antivirus.ScanCommand.Placeholder.REPORT_FILE
import sk.csirt.viruschecker.driver.antivirus.ScanCommand.Placeholder.SCAN_FILE
import sk.csirt.viruschecker.driver.config.Properties.avast
import sk.csirt.viruschecker.driver.config.Properties.eset
import sk.csirt.viruschecker.driver.config.Properties.kaspersky
import sk.csirt.viruschecker.driver.config.Properties.keepReportsDays
import sk.csirt.viruschecker.driver.config.Properties.microsoft
import sk.csirt.viruschecker.driver.config.Properties.scanTimeout
import sk.csirt.viruschecker.driver.utils.cleanCommentsAndEmptyLines
import java.nio.charset.Charset
import java.nio.file.Paths

object PropertiesFactory {

    val propertiesName = "viruschecker-driver.properties"

    private val propertiesFile = Paths.get(propertiesName).toAbsolutePath().toFile()

    private val logger = KotlinLogging.logger { }

    fun loadOrCreateDefault(): Map<String, Any> {
        if (propertiesFile.exists().not()) {
            logger.debug("File $propertiesName not found. Creating default property file.")
            createDefualtProperties()
        }
        return loadProperties()
    }

    private fun createDefualtProperties() {
        val content = """
# Keep records for specified start
$keepReportsDays=30
# Scan socketTimeout
$scanTimeout=15000

# =============================================================================
# Avast

$avast=ashCmd.exe $SCAN_FILE /_> $REPORT_FILE

# =============================================================================
# Eset

$eset=ecls.exe $SCAN_FILE /log-file=$REPORT_FILE /log-all

# =============================================================================
# Kaspersky

$kaspersky=avp.com scan $SCAN_FILE /RA:$REPORT_FILE /i0

# =============================================================================
# Microsoft

$microsoft=MpCmdRun.exe -Scan -ScanType 3 -File $SCAN_FILE -DisableRemediation

"""
        FileUtils.write(propertiesFile, content, Charset.defaultCharset())
    }

    private fun loadProperties(): Map<String, Any> =
        FileUtils.readLines(propertiesFile, Charset.defaultCharset())
            .cleanCommentsAndEmptyLines()
            .associate {
                it.split(limit = 2, delimiters = *arrayOf("=", "= ", " = ")).let {
                    it[0] to
                            (it[1].toDoubleOrNull() ?: it[1].toIntOrNull() ?: it[1])
                }
            }.also { logger.debug("Loaded custom properties: $it") }
}
