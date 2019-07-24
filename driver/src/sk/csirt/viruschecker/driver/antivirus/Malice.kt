//package sk.csirt.viruschecker.driver.antivirusName
//
//import sk.csirt.viruschecker.driver.config.AntivirusType
//import java.io.File
//
//class Malice(
//    scanCommand: RunProgramCommand,
//    updateCommand: RunProgramCommand
//) : UpdatableCommandLineAntivirus(scanCommand, updateCommand) {
//
//    override suspend fun parseReportFile(
//        reportFile: File,
//        params: FileScanParameters
//    ): Sequence<ReportEntry> {
//
//    }
//
//    override val type: AntivirusType
//        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
//
//}