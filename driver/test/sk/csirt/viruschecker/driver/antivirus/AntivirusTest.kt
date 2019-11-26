package sk.csirt.viruschecker.driver.antivirus

internal interface AntivirusTest {
    fun `Healthy file scan test`()

    fun `Infected file scan test`()

    fun `Healthy archive file scan test`()

    fun `Infected archive file scan test`()

    fun `Get virus database version test`()
}