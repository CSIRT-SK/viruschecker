package sk.csirt.viruschecker.gateway.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.config.defaultTimeout
import java.io.File
import java.nio.charset.Charset

class CommandLineArguments(parser: ArgParser) {
    val driverUrls by parser.positional(
        help = "List of urls containing virus checker drivers."
    ) { FileUtils.readLines(File(this), Charset.defaultCharset())
        .map { it.trim() }
        .filterNot { it.startsWith("#") } }

    val socketTimeout by parser.storing(
        "-t", "--timeout",
        help = "Optional: Sets WebSocket socketTimeout in milliseconds. Default is $defaultTimeout."
    ) { this.toLong() }.default(defaultTimeout)
}