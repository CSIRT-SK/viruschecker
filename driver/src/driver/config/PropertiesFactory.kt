package driver.config

import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import utils.cleanCommentsAndEmptyLines
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
keep.reports.days=30
# Update start (24h format)
update.start=03:00
# Update interval
update.start.interval.days=1
# Scan timeout
scan.timeout.millis=15000

# =============================================================================
# Avast

avast=ashCmd.exe
avast.flag.report=/_>

avast.scan.flag=
## Additional flags separated by ', '
avast.scan.flag.additional=

# =============================================================================
# Kaspersky

kaspersky=avp.com
kaspersky.flag.report=/RA:

kaspersky.scan.flag=scan
kaspersky.scan.flag.additional=/i0

kaspersky.update.flag=update
## Additional flags separated by ', '
kaspersky.update.flag.additional=

# =============================================================================
# Eset

eset=ecls.exe
eset.flag.report=/log-file=

eset.scan.flag=
## Additional flags separated by ', '
eset.scan.flag.additional=/log-all
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
