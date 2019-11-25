package sk.csirt.viruschecker.config

import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.utils.filterPropertiesLines
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths

private val logger = KotlinLogging.logger { }

interface PropertiesFactory {

    val propertiesName: String

    val propertiesFile: File get() = Paths.get(propertiesName).toAbsolutePath().toFile()

    val defaultProperties: String

    fun loadOrCreateDefault(): Map<String, Any> {
        if (propertiesFile.exists().not()) {
            logger.debug("File $propertiesName not found. Creating default property file.")
            createDefaultProperties()
        }
        return loadProperties(propertiesFile)
    }

    private fun createDefaultProperties() {
        FileUtils.write(propertiesFile, defaultProperties, Charset.defaultCharset())
    }

    private fun loadProperties(propertiesFile: File): Map<String, Any> =
        FileUtils.readLines(propertiesFile, Charset.defaultCharset())
            .filterPropertiesLines()
            .associate { propertyLine ->
                propertyLine.split(limit = 2, delimiters = *arrayOf("=", "= ", " = ")).let {
                    it[0] to (it[1].toDoubleOrNull() ?: it[1].toIntOrNull() ?: it[1])
                }
            }
}