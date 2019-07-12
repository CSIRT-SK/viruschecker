package sk.csirt.viruschecker.driver.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class CommandLineArguments(parser: ArgParser) {

    val antivirus by parser.mapping(
        "--avast" to AntivirusType.AVAST,
        "--avira" to AntivirusType.AVIRA,
        "--bitdefender" to AntivirusType.BITDEFENDER,
        "--eset" to AntivirusType.ESET,
        "--kaspersky" to AntivirusType.KASPERSKY,
        "--microsoft" to AntivirusType.MICROSOFT,
        "--symantec" to AntivirusType.SYMANTEC,
        help = "Antivirus to use."
    )

    val outputFile by parser.storing(
        "-o", "--out",
        help = "Specify file to store output. Supports also csv format if the filename ends " +
                "with `.csv`."
    ).default("report.csv")

}