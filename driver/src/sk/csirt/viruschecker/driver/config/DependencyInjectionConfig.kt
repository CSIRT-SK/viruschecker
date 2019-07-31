package sk.csirt.viruschecker.driver.config

import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.driver.antivirus.*
import sk.csirt.viruschecker.driver.parsedArgs

internal val defaultAntivirusQualifier = named("antivirus")

val driverDependencyInjectionModule = module {

    single<Antivirus>(AntivirusType.AVAST) {
        Avast(RunProgramCommand(getProperty(Properties.avast)))
    }

    single<Antivirus>(AntivirusType.ESET) {
        Eset(RunProgramCommand(getProperty(Properties.eset)))
    }

    single<Antivirus>(AntivirusType.KASPERSKY) {
        Kaspersky(RunProgramCommand(getProperty(Properties.kaspersky)))
    }

    single<Antivirus>(AntivirusType.MICROSOFT) {
        Microsoft(RunProgramCommand(getProperty(Properties.microsoft)))
    }

    single<Antivirus>(AntivirusType.COMODO) {
        Comodo(RunProgramCommand(getProperty(Properties.comodo)))
    }

    single<Antivirus>(AntivirusType.VIRUS_TOTAL) {
        VirusTotal(getProperty(Properties.virusTotal))
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    single<Antivirus>(defaultAntivirusQualifier) {
        val antivirusTypesToLoad = parsedArgs.antivirusTypes
        if (antivirusTypesToLoad.size == 1) {
            get(antivirusTypesToLoad[0])
        } else {
            ComposedAntivirus(
                antiviruses = antivirusTypesToLoad.map { get<Antivirus>(it) }
            )
        }
    }

}