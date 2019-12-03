package sk.csirt.viruschecker.gateway.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import sk.csirt.viruschecker.config.defaultTimeout
import java.io.File
import java.time.Duration

class CommandLineArguments(parser: ArgParser) {
    val driverUrls by parser.positional(
        help = "List of urls containing virus checker drivers."
    ) {
        File(this)
            .readLines()
            .map { it.trim() }
            .filterNot { it.startsWith("#") }
    }

    val socketTimeout: Duration by parser.storing(
        "-t", "--timeout",
        help = "Optional: Set socket timeout in milliseconds. Default is value " +
                "${defaultTimeout.toMillis()}."
    ) { Duration.ofMillis(this.toLong()) }.default(defaultTimeout)

    val useInMemoryDatabase by parser.flagging(
        "--mem-db",
        help = "Use in memory database rather than persisted to disk."
    )
}