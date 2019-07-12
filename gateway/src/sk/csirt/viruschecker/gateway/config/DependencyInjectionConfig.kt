package sk.csirt.viruschecker.gateway.config

import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.gateway.service.DefaultDriverScanService
import sk.csirt.viruschecker.gateway.service.DriverInfoService
import sk.csirt.viruschecker.gateway.service.ScanService
import sk.csirt.viruschecker.gateway.cache.repository.DummyScanReportRepository
import sk.csirt.viruschecker.gateway.cache.repository.ScanReportRepository
import sk.csirt.viruschecker.gateway.cache.service.ScanReportService
import sk.csirt.viruschecker.gateway.parsedArgs
import sk.csirt.viruschecker.gateway.service.CachedScanService

private val checkedDriverUrls = named("checked.driver.urls")

val gatewayDependencyInjectionModule = module {
    single<ScanReportRepository> { DummyScanReportRepository }
    single { ScanReportService(get()) }
    single { httpClient(parsedArgs.socketTimeout.toInt()) }
    single { DriverInfoService(parsedArgs.driverUrls, get()) }
    single(checkedDriverUrls) {
        runBlocking { get<DriverInfoService>().info() }
            .filter { it.success }
            .map { it.url }
    }
    single { DefaultDriverScanService(get(checkedDriverUrls), get()) }
    single { CachedScanService(get(), get()) }

}