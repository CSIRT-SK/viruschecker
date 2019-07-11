package sk.csirt.viruschecker.client.web.config

import org.koin.dsl.module
import sk.csirt.viruschecker.client.config.httpClient
import sk.csirt.viruschecker.client.service.AntivirusDriverInfoService
import sk.csirt.viruschecker.client.service.ScanService
import sk.csirt.viruschecker.client.web.parsedArgs
import sk.csirt.viruschecker.client.web.repository.DummyScanReportRepository
import sk.csirt.viruschecker.client.web.repository.ScanReportRepository
import sk.csirt.viruschecker.client.web.service.ScanReportService

val webClientDependencyInjectionModule = module {
    single<ScanReportRepository> { DummyScanReportRepository }
    single { ScanReportService(get()) }
    single { httpClient(parsedArgs.timeout) }
    single { ScanService(parsedArgs.driverUrls, get()) }
    single { AntivirusDriverInfoService(parsedArgs.driverUrls, get()) }
}