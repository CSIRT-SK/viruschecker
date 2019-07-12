package sk.csirt.viruschecker.client.web.config

import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.client.config.httpClient
import sk.csirt.viruschecker.client.service.AntivirusDriverInfoService
import sk.csirt.viruschecker.client.service.DefaultScanService
import sk.csirt.viruschecker.client.web.parsedArgs
import sk.csirt.viruschecker.client.web.repository.DummyScanReportRepository
import sk.csirt.viruschecker.client.web.repository.ScanReportRepository
import sk.csirt.viruschecker.client.web.service.ScanReportService

private val checkedDriverUrls = named("checked.driver.urls")

val webClientDependencyInjectionModule = module {
    single<ScanReportRepository> { DummyScanReportRepository }
    single { ScanReportService(get()) }
    single { httpClient(parsedArgs.timeout) }
    single { AntivirusDriverInfoService(parsedArgs.driverUrls, get()) }
    single(checkedDriverUrls) {
        runBlocking { get<AntivirusDriverInfoService>().info() }
            .filter { it.success }
            .map { it.url }
    }
    single { DefaultScanService(get(checkedDriverUrls), get()) }

}