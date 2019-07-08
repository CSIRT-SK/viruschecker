package sk.csirt.viruschecker.driver.antivirus

interface UpdatableAntivirus : Antivirus {
    fun update() : String
}