package sk.csirt.viruschecker.gateway.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class CommandLineArguments(parser: ArgParser) {

    val driverUrls by parser.positionalList(
        help = "List of urls containing virus checker drivers."
    )

    private val defaultTimeut = 40000
    val timeout by parser.storing(
        "-t", "--timeout",
        help = "Optional: Sets socket timeout in milliseconds. Default is $defaultTimeut."
    ).default(defaultTimeut)

}