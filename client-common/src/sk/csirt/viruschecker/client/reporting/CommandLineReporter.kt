package sk.csirt.viruschecker.client.reporting

import sk.csirt.viruschecker.client.payload.FileMultiScanResponse

class CommandLineReporter : Reporter {

    override fun saveReport(result: FileMultiScanResponse) {

//        val fileInfoHead = "| "
//        val fileInfoLine = "| ${result.filename} | ${result.date} |"
//
//
//        val dashedBorded = "-".repeat(
//            result.filename.length +
//                    result.date.toString().length
//        )
//
//        synchronized(System.out) {
//            println()
//            println(dashedBorded)
//            println("| ${resultStrings.joinToString(" | ")} |")
//            println(dashedBorded)
//        }
//        val resultStrings = results.map { it.toString() }

//        val dashedBorded = "-".repeat(
//            resultStrings.map { it.length }.sum() +
//                    (resultStrings.size - 1) * 3 + 4
//        )
//
//        synchronized(System.out) {
//            println()
//            println(dashedBorded)
//            println("| ${resultStrings.joinToString(" | ")} |")
//            println(dashedBorded)
//        }
    }

}
