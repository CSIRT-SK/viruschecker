package sk.csirt.viruschecker.driver.config

import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.driver.antivirus.*
import sk.csirt.viruschecker.driver.parsedArgs
import sk.csirt.viruschecker.driver.utils.ProcessRunner
import sk.csirt.viruschecker.driver.utils.WindowsRegistry

internal val defaultAntivirusQualifier = named("antivirus")

val driverDependencyInjectionModule = module {

    single {
        ProcessRunner()
    }

    single {
        WindowsRegistry()
    }


    single<Antivirus>(AntivirusType.DUMMY) {
        DummyAntivirus()
    }

    single<Antivirus>(AntivirusType.AVAST) {
        Avast(
            RunProgramCommand(getProperty(Properties.avast)),
            get()
        )
    }

    single<Antivirus>(AntivirusType.ESET) {
        Eset(
            RunProgramCommand(getProperty(Properties.eset)),
            get()
        )
    }

    single<Antivirus>(AntivirusType.KASPERSKY) {
        Kaspersky(
            RunProgramCommand(getProperty(Properties.kaspersky)),
            get()
        )
    }

    single<Antivirus>(AntivirusType.MICROSOFT) {
        Microsoft(
            RunProgramCommand(getProperty(Properties.microsoft)),
            get(),
            get()
        )
    }

    single<Antivirus>(AntivirusType.COMODO) {
        Comodo(
            RunProgramCommand(getProperty(Properties.comodo)),
            get()
        )
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