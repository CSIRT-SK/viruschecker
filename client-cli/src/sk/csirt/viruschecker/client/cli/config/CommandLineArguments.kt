package sk.csirt.viruschecker.client.cli.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset

class CommandLineArguments(parser: ArgParser) {
    val driverUrls by parser.positional(
        help = "List of urls containing virus checker drivers."
    ) { FileUtils.readLines(File(this), Charset.defaultCharset())
        .map { it.trim() }
        .filterNot { it.startsWith("#") } }

    val fileToScan by parser.positional(
        help = "File to scan. Does not support directories (only archived)."
    ) { File(this) }

    private val defaultTimeut = 50000
    val timeout by parser.storing(
        "-t", "--timeout",
        help = "Optional: Sets socket timeout in milliseconds. Default is $defaultTimeut."
    ) { this.toInt() }.default(defaultTimeut)

    val outputFile by parser.storing(
        "-o", "--out",
        help = "Specify file to store report. Supports also csv and Markdown if the filename ends" +
                " with appropriate postfix."
    ) { File(this) }.default<File?>(null)
}