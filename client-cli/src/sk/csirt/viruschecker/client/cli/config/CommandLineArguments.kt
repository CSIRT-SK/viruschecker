package sk.csirt.viruschecker.client.cli.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

class CommandLineArguments(parser: ArgParser) {
    val gatewayUrl by parser.positional(
        help = "Url for virus checker gateway."
    )

    val fileToScan by parser.positional(
        help = "File to scan. Does not support directories (only archived)."
    ) { File(this) }

    private val defaultTimeut = 50000
    val timeout by parser.storing(
        "-t", "--timeout",
        help = "Optional: Sets socket timeout in milliseconds. Default is $defaultTimeut."
    ).default(defaultTimeut)


    val outputFile by parser.storing(
        "-o", "--out",
        help = "Specify file to store report. Supports also csv and Markdown if the filename ends" +
                " with appropriate postfix."
    ) { File(this) }.default<File?>(null)
}