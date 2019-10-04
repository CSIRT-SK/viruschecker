package sk.csirt.viruschecker.client.web.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import sk.csirt.viruschecker.config.defaultTimeout
import java.time.Duration

class CommandLineArguments(parser: ArgParser) {
    val gateway by parser.positional(
        help = "Gateway url."
    )

    val socketTimeout: Duration by parser.storing(
        "-t", "--timeout",
        help = "Optional: Set socket timeout in milliseconds. Default is value " +
                "${defaultTimeout.toMillis()}."
    ) { Duration.ofMillis(this.toLong()) }.default(defaultTimeout)

}