package sk.csirt.viruschecker.client.web.config

import org.koin.dsl.module
import sk.csirt.viruschecker.client.service.ClientScanService
import sk.csirt.viruschecker.client.service.GatewayInfoService
import sk.csirt.viruschecker.client.service.GatewayReportService
import sk.csirt.viruschecker.client.web.parsedArgs
import sk.csirt.viruschecker.config.httpClient

val webClientDependencyInjectionModule = module {
    single { httpClient(parsedArgs.socketTimeout) }
    single { GatewayInfoService(parsedArgs.gateway, get()) }
    single { GatewayReportService(parsedArgs.gateway, get()) }
    single { ClientScanService(parsedArgs.gateway, get()) }
}