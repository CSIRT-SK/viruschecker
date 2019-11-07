package sk.csirt.viruschecker.driver.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import sk.csirt.viruschecker.config.defaultTimeout
import java.time.Duration

class CommandLineArguments(parser: ArgParser) {

    val antivirusTypes by parser.positionalList(
        help = "Antivirus plugins to load. Possible values are: " +
                AntivirusType.values().joinToString(separator = ", ") {
                    it.name
                },
        sizeRange = 0..AntivirusType.values().size
    ){
        AntivirusType.valueOf(this)
    }

    val autodetectAntiviruses by parser.flagging(
        "-a", "--auto-detect", "--auto",
        help = "Enable auto detection of installed AVs. AVs with command line scanners must be in " +
                "the Path."
    )

    val socketTimeout: Duration by parser.storing(
        "-t", "--timeout",
        help = "Optional: Set socket timeout in milliseconds. Default is value " +
                "${defaultTimeout.toMillis()}."
    ) { Duration.ofMillis(this.toLong()) }.default(defaultTimeout)
}