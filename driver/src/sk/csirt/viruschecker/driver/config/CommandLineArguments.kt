package sk.csirt.viruschecker.driver.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import sk.csirt.viruschecker.config.defaultTimeout

class CommandLineArguments(parser: ArgParser) {
    val antivirus by parser.mapping(
        *AntivirusType.values().map {
            "--${it.antivirusName.toLowerCase()}" to it
        }.toTypedArray(),
        help = "Antivirus to use."
    )
}