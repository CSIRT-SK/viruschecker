package sk.csirt.viruschecker.client.web.config

import org.koin.dsl.module
import sk.csirt.viruschecker.client.config.httpClient
import sk.csirt.viruschecker.client.service.DriverInfoGatewayService
import sk.csirt.viruschecker.client.service.MultiScanService
import sk.csirt.viruschecker.client.service.ReportByHashService
import sk.csirt.viruschecker.client.web.parsedArgs

val webClientDependencyInjectionModule = module {
    single { httpClient(parsedArgs.socketTimeout.toInt()) }
    single { DriverInfoGatewayService(parsedArgs.gateway, get()) }
    single { ReportByHashService(parsedArgs.gateway, get()) }
    single { MultiScanService(parsedArgs.gateway, get()) }
}