package sk.csirt.viruschecker.gateway.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val defaultAntivirusQualifier = named("antivirus")

val testDriverDependencyInjectionModule = module {

}
