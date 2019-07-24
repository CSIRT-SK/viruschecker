package sk.csirt.viruschecker.driver.config

import org.koin.dsl.module
import sk.csirt.viruschecker.driver.antivirus.*

val driverDependencyInjectionModule = module {

    single<Antivirus>(AntivirusType.AVAST) {
        Avast(RunProgramCommand(getProperty(Properties.Windows.avast)))
    }

    single<Antivirus>(AntivirusType.ESET) {
        Eset(RunProgramCommand(getProperty(Properties.Windows.eset)))
    }

    single<Antivirus>(AntivirusType.KASPERSKY) {
        Kaspersky(RunProgramCommand(getProperty(Properties.Windows.kaspersky)))
    }

    single<Antivirus>(AntivirusType.MICROSOFT) {
        Microsoft(RunProgramCommand(getProperty(Properties.Windows.microsoft)))
    }

    single<Antivirus>(AntivirusType.COMODO) {
        Comodo(RunProgramCommand(getProperty(Properties.Linux.comodo)))
    }

    single<Antivirus>(AntivirusType.VIRUS_TOTAL) {
        VirusTotal(getProperty(Properties.virusTotal))
    }

}