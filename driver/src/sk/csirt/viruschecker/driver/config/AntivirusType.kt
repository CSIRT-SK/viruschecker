package sk.csirt.viruschecker.driver.config

import org.koin.core.qualifier.Qualifier

enum class AntivirusType(
        val antivirusName: String
): Qualifier {
    AVAST("Avast"),
    COMODO("Comodo"),
    ESET("Eset"),
    KASPERSKY("Kaspersky"),
    MICROSOFT("Microsoft"),
    VIRUS_TOTAL("VirusTotal"),
}

