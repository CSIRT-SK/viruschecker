package sk.csirt.viruschecker.driver.config

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

    override val propertiesName = "viruschecker-driver.properties"

    const val missingApiKeyPlaceHolder = "<insert-your-api-key>"

    override val defaultProperties by lazy {
        if (SystemUtils.IS_OS_WINDOWS) defaultPropertiesWindows
        else defaultPropertiesUnix
    }

}
