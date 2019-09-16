package sk.csirt.viruschecker.gateway.config

import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.gateway.parsedArgs
import sk.csirt.viruschecker.gateway.persistence.ScanReportDatabase
import sk.csirt.viruschecker.gateway.persistence.entity.AntivirusReportEntity
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.gateway.persistence.repository.KeyValueScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.repository.ScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.service.PersistentScanReportService
import sk.csirt.viruschecker.gateway.routing.service.CachedDriverScanService
import sk.csirt.viruschecker.gateway.routing.service.DefaultDriverScanService
import sk.csirt.viruschecker.gateway.routing.service.DriverInfoService
import sk.csirt.viruschecker.routing.payload.UrlDriverInfoResponse
import java.io.File
import java.time.Instant

internal val checkedDriverUrls = named("checked.driver.urls")
private val database = named("scan.report.database")

val gatewayDependencyInjectionModule = module {
    val sha256Sample = "2f66755d1d48dab1d49441087077cdb4ef4161dca5fb536446c2c20d1ee596fb"
    val sha1Sample = "27cdb0eb9e1fe3fb6a91cec02e2b21c8251e091c"
    val md5Sample = "597c538cf3ea4f2f97cb1b9481af443e"
    single(database) {
        ScanReportDatabase(
            file = File(getProperty<String>("database.file")),
            entries = getProperty("database.default.size"),
            sampleKey = sha256Sample,
            sampleValue = ScanReportEntity(
                sha256 = sha256Sample,
                filename = "thisFileDoesNotExists.txt",
                date = Instant.now(),
                md5 = md5Sample,
                sha1 = sha1Sample,
                reports = (1..7).map {
                    AntivirusReportEntity(
                        antivirus = "Antivirus-$it",
                        status = "INFECTED",
                        malwareDescription = "This is a super duper malware",
                        virusDatabaseVersion = "database-2019-09-09"
                    )
                }
            ),
            name = database.value
        )
    }

    single<ScanReportRepository> { KeyValueScanReportRepository(get(database)) }
    single { PersistentScanReportService(get()) }
    single { httpClient(parsedArgs.socketTimeout) }
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
            get<List<UrlDriverInfoResponse>>(checkedDriverUrls).map{ it.url },
            get()
        )
    }
    single { CachedDriverScanService(get(), get()) }

}