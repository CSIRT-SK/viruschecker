package driver.antivirus

interface UpdatableAntivirus : Antivirus {
    fun update() : String
}