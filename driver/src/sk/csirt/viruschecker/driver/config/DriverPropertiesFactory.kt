package sk.csirt.viruschecker.driver.config

import mu.KotlinLogging
import org.apache.commons.lang3.SystemUtils
import sk.csirt.viruschecker.config.PropertiesFactory

object Properties {
    const val avast = "avast"
    const val eset = "eset"
    const val kaspersky = "kaspersky"
    const val microsoft = "microsoft"
    const val comodo = "comodo"
    const val virusTotal = "virustotal.apikey"
}

object DriverPropertiesFactory : PropertiesFactory {
    private val logger = KotlinLogging.logger { }
    override val propertiesName = "viruschecker-driver.properties"
    // Do not change the value of this variable! Insert your key into [defaultPropertiesUnix] or
    // [defaultPropertiesWindows].
    const val missingApiKeyPlaceHolder = "<insert-your-api-key>"
    override val defaultProperties by lazy {
        when {
            SystemUtils.IS_OS_WINDOWS -> {
                logger.debug("Operating system Windows detected.")
                defaultPropertiesWindows
            }
            SystemUtils.IS_OS_UNIX -> {
                logger.debug("Operating system Linux detected.")
                defaultPropertiesUnix
            }
            else -> {
                logger.warn(
                    "Operating system is not officially supported. Generating empty " +
                            "$propertiesName file."
                )
                ""
            }
        }
    }
}
