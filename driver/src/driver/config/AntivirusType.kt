package driver.config

import org.koin.core.qualifier.Qualifier

enum class AntivirusType(
        val commonName: String
): Qualifier {
    AVAST("Avast"),
    AVIRA("Avira"),
    BITDEFENDER("Bitdefender"),
    ESET("Eset"),
    KASPERSKY("Kaspersky"),
    MS_DEFENDER("Microsoft Defender"),
    NORTON("Norton"),
}

