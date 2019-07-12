package sk.csirt.viruschecker.driver.config

import org.koin.dsl.module
import sk.csirt.viruschecker.driver.antivirus.*

object Properties {

    val keepReportsDays = "keep.reports.days"
    val scanTimeout = "scan.socketTimeout.millis"

    val avast = "avast"
    val eset = "eset"
    val kaspersky = "kaspersky"
    val microsoft = "microsoft"
}

val driverDependencyInjectionModule = module {

    single<Antivirus>(AntivirusType.AVAST) {
        Avast(ScanCommand(getProperty(Properties.avast)))
    }

    single<Antivirus>(AntivirusType.ESET) {
        Eset(ScanCommand(getProperty(Properties.eset)))
    }

    single<Antivirus>(AntivirusType.KASPERSKY) {
        Kaspersky(ScanCommand(getProperty(Properties.kaspersky)))
    }

    single<Antivirus>(AntivirusType.MICROSOFT) {
        Microsoft(ScanCommand(getProperty(Properties.microsoft)))
    }

}