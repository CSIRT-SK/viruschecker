package sk.csirt.viruschecker.driver.antivirus

interface AntivirusTest {
    fun `Healthy file scan test`()

    fun `Infected file scan test`()

    fun `Healthy archive file scan test`()

    fun `Infected archive file scan test`()

    fun `Get virus database version test`()
}