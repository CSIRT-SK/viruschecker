package sk.csirt.viruschecker.gateway.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger { }

class Database(
    config: ApplicationConfig? = null
) {

    init {
        Database.connect(hikari(config))
        transaction {
            SchemaUtils.createMissingTablesAndColumns(ScanReports, AntivirusReportItems)
        }
    }

    @KtorExperimentalAPI
    private fun hikari(config: ApplicationConfig?): HikariDataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = config?.propertyOrNull("db.driver")?.getString() ?: "org.h2.Driver"
        hikariConfig.jdbcUrl = config?.propertyOrNull("db.jdbcUrl")?.getString() ?: "jdbc:h2:mem:test"
        hikariConfig.username = config?.propertyOrNull("db.username")?.getString()
        hikariConfig.password = config?.propertyOrNull("db.password")?.getString()
        hikariConfig.maximumPoolSize = 3
        hikariConfig.isAutoCommit = false
        hikariConfig.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        hikariConfig.validate()
        return HikariDataSource(hikariConfig)
    }

    suspend fun <T> query(block: Transaction.() -> T): T = withContext(IO) {
        transaction {
            block()
        }
    }
}
