package sk.csirt.viruschecker.client.web.config

import org.koin.dsl.module
import sk.csirt.viruschecker.client.config.httpClient
import sk.csirt.viruschecker.client.service.GatewayInfoService
import sk.csirt.viruschecker.client.service.GatewayScanService
import sk.csirt.viruschecker.client.service.GatewayReportService
import sk.csirt.viruschecker.client.web.parsedArgs

val webClientDependencyInjectionModule = module {
    single { httpClient(parsedArgs.socketTimeout) }
    single { GatewayInfoService(parsedArgs.gateway, get()) }
    single { GatewayReportService(parsedArgs.gateway, get()) }
    single { GatewayScanService(parsedArgs.gateway, get()) }
}