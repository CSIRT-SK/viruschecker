package sk.csirt.viruschecker.gateway.config

import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respondOk
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import sk.csirt.viruschecker.gateway.parsedArgs
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.gateway.persistence.repository.KeyValueScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.repository.ScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
import sk.csirt.viruschecker.gateway.routing.service.CachedDriverScanService
import sk.csirt.viruschecker.gateway.routing.service.DefaultDriverScanService
import sk.csirt.viruschecker.gateway.routing.service.DriverInfoService
import sk.csirt.viruschecker.routing.payload.UrlDriverInfoResponse
import java.util.concurrent.ConcurrentHashMap

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun testGatewayDependencyInjectionModule(
    httpClientRequestHandler: MockRequestHandler = { respondOk() }
) = module {

    single<ScanReportRepository> {
        KeyValueScanReportRepository(ConcurrentHashMap<String, ScanReportEntity>())
    }
    single { PersistentScanReportService(get()) }
    single { mockHttpClient(httpClientRequestHandler) }
    single {
        DriverInfoService(
            parsedArgs.driverUrls,
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
}
