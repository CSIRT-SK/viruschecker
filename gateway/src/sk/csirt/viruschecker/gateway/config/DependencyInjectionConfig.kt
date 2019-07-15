package sk.csirt.viruschecker.gateway.config

import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.gateway.routing.service.DefaultDriverScanService
import sk.csirt.viruschecker.gateway.routing.service.DriverInfoService
import sk.csirt.viruschecker.gateway.persistence.repository.ScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.service.ScanReportService
import sk.csirt.viruschecker.gateway.parsedArgs
import sk.csirt.viruschecker.gateway.persistence.ScanReportDatabase
import sk.csirt.viruschecker.gateway.persistence.entity.AntivirusReportEntity
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.gateway.persistence.repository.KeyValueScanReportRepository
import sk.csirt.viruschecker.gateway.routing.service.CachedDriverScanService
import sk.csirt.viruschecker.hash.HashHolder
import java.io.File
import java.time.Instant

private val checkedDriverUrls = named("checked.driver.urls")
private val database = named("scan.report.database")

val gatewayDependencyInjectionModule = module {
    val sha256Sample = "f87a48a0f14418b6b49a17d9616cfa0385ab7339cbb4f5b602f48a7a9ea71bcd"
    single(database) {
        ScanReportDatabase(
            file = File(getProperty<String>("database.file")),
            entries = getProperty("database.default.size"),
            sampleKey = sha256Sample,
            sampleValue = ScanReportEntity(
                sha256 = sha256Sample,
                filename = "thisFileDoesNotExists.txt",
                date = Instant.now(),
                otherHashes = listOf(
                    HashHolder(
                        value = "f4c7ed3d8c5d0ca5ff98b22bfbf977fc",
                        algorithm = "MD5"
                    )
                ),
                reports = (1..7).map {
                    AntivirusReportEntity(
                        antivirus = "Antivirus-$it",
                        status = "INFECTED",
                        malwareDescription = "This is a super duper malware"
                    )
                }
            ),
            name = database.value
        )
    }
    single<ScanReportRepository> { KeyValueScanReportRepository(get(database)) }
    single { ScanReportService(get()) }
    single { httpClient(parsedArgs.socketTimeout) }
    single { DriverInfoService(parsedArgs.driverUrls, get()) }
    single(checkedDriverUrls) {
        runBlocking { get<DriverInfoService>().info() }
            .filter { it.success }
            .map { it.url }
    }
    single { DefaultDriverScanService(get(checkedDriverUrls), get()) }
    single { CachedDriverScanService(get(), get()) }

}