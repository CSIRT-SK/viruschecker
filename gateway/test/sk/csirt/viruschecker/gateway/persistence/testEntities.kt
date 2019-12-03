package sk.csirt.viruschecker.gateway.persistence

import kotlinx.coroutines.runBlocking
import sk.csirt.viruschecker.gateway.persistence.entity.AntivirusReportEntity
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.hash.HashAlgorithm
import sk.csirt.viruschecker.routing.payload.ScanStatus
import java.time.Instant

internal val fileContent1 = "HalaBalaTancovala"
internal val fileContent2 = "ANaUstaSpadla"

internal val testScanReportEntity1 = runBlocking {
    ScanReportEntity(
        sha256 = HashAlgorithm.Sha256().hash(fileContent1),
        md5 = HashAlgorithm.Md5().hash(fileContent1),
        sha1 = HashAlgorithm.Sha1().hash(fileContent1),
        date = Instant.now(),
        filename = "CervenaKarkulka",
        reports = listOf(
            AntivirusReportEntity(
                antivirus = "MeskaVlakUz50Minut",
                malwareDescription = "SkodaZeTrasou-BA-KE-UzNejazdiaAlternativnyDopravcovia",
                virusDatabaseVersion = "LeboMeskanieJeSposobenePoruchouVozna",
                status = ScanStatus.INFECTED
            ),
            AntivirusReportEntity(
                antivirus = "Te-kila",
                malwareDescription = "NalejBoVypito",
                virusDatabaseVersion = "VypiBoNalato",
                status = ScanStatus.INFECTED
            )
        )
    )
}

internal val testScanReportEntity2 = runBlocking {
    ScanReportEntity(
        sha256 = HashAlgorithm.Sha256().hash(fileContent2),
        md5 = HashAlgorithm.Md5().hash(fileContent2),
        sha1 = HashAlgorithm.Sha1().hash(fileContent2),
        date = Instant.now(),
        filename = "FialovaKarkulka",
        reports = listOf(
            AntivirusReportEntity(
                antivirus = "UzZaseStojimeZPrevadzkovychDovodov",
                malwareDescription = "TenVlakViacStojiAkoIde...Prisamvacku",
                virusDatabaseVersion = "PriemerneKazdych8MinutStojimeZPrevadzkovychDovodov",
                status = ScanStatus.INFECTED
            ),
            AntivirusReportEntity(
                antivirus = "MalBySomZacatLogovatZastavenia",
                malwareDescription = "AleToByMaPriPohladeNaToNeskorDefinitvneTrafilSlak",
                virusDatabaseVersion = "RadsejBudemKoditNechAsponNejakVyuzijemTenUzasnyCasNaviac",
                status = ScanStatus.INFECTED
            )
        )
    )
}

internal val testScanReportEntities = listOf(
    testScanReportEntity1,
    testScanReportEntity2
)