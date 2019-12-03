package sk.csirt.viruschecker.gateway.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.config.defaultTimeout
import sk.csirt.viruschecker.config.httpClient
import sk.csirt.viruschecker.gateway.persistence.Database
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.gateway.persistence.repository.KeyValueScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.repository.SqlScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
import sk.csirt.viruschecker.gateway.routing.service.CachedDriverScanService
import sk.csirt.viruschecker.gateway.routing.service.DefaultDriverScanService
import sk.csirt.viruschecker.gateway.routing.service.DriverInfoService
import sk.csirt.viruschecker.routing.payload.UrlDriverInfoResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

internal val checkedDriverUrls = named("checked.driver.urls")

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun gatewayDependencyInjectionModule(
    driverUrls: List<String>,
    config: ApplicationConfig,
    defaultSocketTimeout: Duration = defaultTimeout,
    isPersistedDatabase: Boolean = true
) = module {

    single {
        Database(config)
    }

    single {
        if (isPersistedDatabase) {
            SqlScanReportRepository(get())
        } else {
            KeyValueScanReportRepository(ConcurrentHashMap<String, ScanReportEntity>())
        }
    }

    single { PersistentScanReportService(get()) }
    single { httpClient(defaultSocketTimeout) }
    single {
        DriverInfoService(
            driverUrls,
            get()
        )
    }

    single(checkedDriverUrls) {
        runBlocking { get<DriverInfoService>().info() }
            .filter { it.success }
    }

    single {
        DefaultDriverScanService(
            get<List<UrlDriverInfoResponse>>(checkedDriverUrls).map { it.url },
            get()
        )
    }
    single { CachedDriverScanService(get(), get()) }

//    single { ShareService() }
}