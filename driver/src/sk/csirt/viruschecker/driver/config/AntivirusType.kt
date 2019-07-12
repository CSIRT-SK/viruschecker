package sk.csirt.viruschecker.driver.config

import org.koin.core.qualifier.Qualifier

enum class AntivirusType(
        val commonName: String
): Qualifier {
    AVAST("Avast"),
    AVIRA("Avira"),
    BITDEFENDER("Bitdefender"),
    ESET("Eset"),
    KASPERSKY("Kaspersky"),
    MICROSOFT("Microsoft"),
    SYMANTEC("Symantec"),
}

