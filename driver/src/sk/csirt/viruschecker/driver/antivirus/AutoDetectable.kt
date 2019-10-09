package sk.csirt.viruschecker.driver.antivirus

interface AutoDetectable {
    suspend fun isInstalled(): Boolean
}