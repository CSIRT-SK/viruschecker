package sk.csirt.viruschecker.driver.config

import org.koin.core.qualifier.Qualifier

enum class AntivirusType(
        val antivirusName: String
): Qualifier {
    AVAST("Avast"),
    AVIRA("Avira"),
    BITDEFENDER("Bitdefender"),
    COMODO("Comodo"),
    ESET("Eset"),
    KASPERSKY("Kaspersky"),
    MICROSOFT("Microsoft"),
    SYMANTEC("Symantec"),
    VIRUS_TOTAL("VirusTotal"),
}

