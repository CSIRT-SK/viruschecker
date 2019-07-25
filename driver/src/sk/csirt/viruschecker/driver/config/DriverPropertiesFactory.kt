package sk.csirt.viruschecker.driver.config

import org.apache.commons.lang3.SystemUtils
import sk.csirt.viruschecker.config.PropertiesFactory

object Properties {
    const val keepReportsDays = "keep.results.days"

    object Windows {
        const val avast = "avast.windows"
        const val eset = "eset.windows"
        const val kaspersky = "kaspersky.windows"
        const val microsoft = "microsoft.windows"
    }

    object Linux {
        const val comodo = "comodo.linux"
    }

    const val virusTotal = "virustotal.apikey"
}

object DriverPropertiesFactory : PropertiesFactory {

    override val propertiesName = "viruschecker-driver.properties"

    override val defaultProperties by lazy {
        if(SystemUtils.IS_OS_WINDOWS) defaultPropertiesWindows
        else defaultPropertiesUnix
    }

}
