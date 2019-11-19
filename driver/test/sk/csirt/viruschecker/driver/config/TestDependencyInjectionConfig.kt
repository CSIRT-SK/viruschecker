package sk.csirt.viruschecker.sk.csirt.viruschecker.driver.config

import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.driver.antivirus.Antivirus
import sk.csirt.viruschecker.driver.antivirus.DummyAntivirus
import sk.csirt.viruschecker.driver.config.AntivirusType

internal val defaultAntivirusQualifier = named("antivirus")

val testDriverDependencyInjectionModule = module {
    single<Antivirus>(AntivirusType.DUMMY) {
        DummyAntivirus()
    }

    single<Antivirus>(sk.csirt.viruschecker.driver.config.defaultAntivirusQualifier) {
       get(AntivirusType.DUMMY)
    }
}
