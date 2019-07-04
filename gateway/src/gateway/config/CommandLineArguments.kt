package gateway.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

class CommandLineArguments(parser: ArgParser) {

    val fileToScan by parser.positional(
        name = "FILE",
        help = "File to scan. Does not support directories (only archived)."
    ) { File(this) }

    val driverUrls by parser.storing(
        "--connect",
        "-c",
        help = "List of urls containing virus checker drivers. Each url must be separated by `,` without any whitespaces."
    ) { split(",") }



    val outputFile by parser.storing(
        "-o", "--out",
        help = "Specify file to store report. Supports also csv and Markdown if the filename ends" +
                " with appropriate postfix."
    ).default("report.csv")

}