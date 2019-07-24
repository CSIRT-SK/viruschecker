package sk.csirt.viruschecker.driver.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import sk.csirt.viruschecker.config.defaultTimeout

class CommandLineArguments(parser: ArgParser) {

    val antivirus by parser.mapping(
        "--avast" to AntivirusType.AVAST,
        "--avira" to AntivirusType.AVIRA,
        "--bitdefender" to AntivirusType.BITDEFENDER,
        "--comodo" to AntivirusType.COMODO,
        "--eset" to AntivirusType.ESET,
        "--kaspersky" to AntivirusType.KASPERSKY,
        "--microsoft" to AntivirusType.MICROSOFT,
        "--symantec" to AntivirusType.SYMANTEC,
        "--virustotal" to AntivirusType.VIRUS_TOTAL,
        help = "Antivirus to use."
    )

    val outputFile by parser.storing(
        "-o", "--out",
        help = "Specify file to store output. Supports also csv format if the filename ends " +
                "with `.csv`."
    ).default("report.csv")

    val socketTimeout by parser.storing(
        "-t", "--timeout",
        help = "Optional: Set socket timeout in milliseconds. Default is value " +
                "${defaultTimeout.toMillis()}."
    ) { this.toLong() }.default(defaultTimeout)
}