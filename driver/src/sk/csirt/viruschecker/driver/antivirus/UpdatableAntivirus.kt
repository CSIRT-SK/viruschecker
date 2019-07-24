package sk.csirt.viruschecker.driver.antivirus

interface UpdatableAntivirus : Antivirus {
    suspend fun update() : String
}