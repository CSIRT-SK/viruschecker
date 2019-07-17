package sk.csirt.viruschecker.client.cli.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.config.defaultTimeout
import java.io.File
import java.nio.charset.Charset
import java.time.Duration

class CommandLineArguments(parser: ArgParser) {
    val gateway by parser.positional(
        "URL",
        help = "Gateway url."
    )

    val fileToScan by parser.positional(
        help = "File to scan. Does not support directories (only archived)."
    ) { File(this) }

    val socketTimeout by parser.storing(
        "-t", "--timeout",
        help = "Optional: Sets socket timeout in milliseconds. Default value is " +
                "${defaultTimeout.toMillis()}."
    ) { Duration.ofMillis(this.toLong()) }.default(defaultTimeout)

    val outputFile by parser.storing(
        "-o", "--out",
        help = "Specify file to store report. Csv format is supported if the filename ends" +
                " with appropriate postfix."
    ) { File(this) }.default<File?>(null)
}