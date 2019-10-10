package sk.csirt.viruschecker.driver.config

import kotlinx.coroutines.runBlocking
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
        val autodetect = parsedArgs.autodetectAntiviruses

        fun autoLoadAntiviruses(): List<Antivirus> {
            logger.debug { "Auto-detection enabled." }
            return runBlocking {
                AntivirusType.values()
                    .mapNotNull {
                        runCatching { get<Antivirus>(it) }.getOrNull()
                    }
                    .filter {
                        (it as? AutoDetectable)?.isInstalled() ?: false
                    }
            }.also {
                logger.debug { "Auto-detected ${it.size} antivirus drivers: $it" }
            }
        }

        val antiviruses = mutableListOf<Antivirus>()
        if (autodetect) {
            antiviruses += autoLoadAntiviruses()
        }
        if (antivirusTypesToLoad.isNotEmpty()) {
            antiviruses += antivirusTypesToLoad.map { get<Antivirus>(it) }
        }

        ComposedAntivirus(
            antiviruses = antiviruses.distinctBy { it.antivirusName }.also {
                logger.info { "Total loaded antivirus drivers ${it.size}: $it" }
            }
        )
    }

}