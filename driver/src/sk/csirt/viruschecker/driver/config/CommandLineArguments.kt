package sk.csirt.viruschecker.driver.config

import com.xenomachina.argparser.ArgParser

class CommandLineArguments(parser: ArgParser) {

    val antivirus by parser.mapping(
        *AntivirusType.values().map {
            "--${it.antivirusName.toLowerCase()}" to it
        }.toTypedArray(),
        help = "Antivirus to use."
    )
}