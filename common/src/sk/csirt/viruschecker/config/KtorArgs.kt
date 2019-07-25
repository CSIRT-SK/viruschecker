package sk.csirt.viruschecker.config

private val argParserIgnore = listOf(
    "-config",
    "-host",
    "-port",
    "-watch"
)

fun filterArgsForArgParser(args: Array<String>): Array<String> =
    args.filterNot { it.split("=")[0] in argParserIgnore }.toTypedArray()