package sk.csirt.viruschecker.client

import sk.csirt.viruschecker.routing.payload.AntivirusReportResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse
import sk.csirt.viruschecker.routing.payload.FileScanResponse
import sk.csirt.viruschecker.routing.payload.ScanStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

internal val instant = Instant.now()

internal val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

internal val testScanResponse = FileHashScanResponse(
    md5 = "69630e4574ec6798239b091cda43dca0".toUpperCase(),
    sha1 = "cf8bd9dfddff007f75adf4c2be48005cea317c62".toUpperCase(),
    sha256 = "131f95c51cc819465fa1797f6ccacf9d494aaaff46fa3eac73ae63ffbdfd8267".toUpperCase(),
    report = FileScanResponse(
        date = instant,
        status = ScanStatus.INFECTED,
        filename = "eicar.txt",
        results = listOf(
            AntivirusReportResponse(
                antivirus = "ClamAV",
                status = ScanStatus.INFECTED,
                malwareDescription = "Eicar-TEST-File",
                virusDatabaseVersion = "1.1.1701"
            )
        )
    )
)

internal val testScanResponseList = listOf(
    testScanResponse,
    FileHashScanResponse(
        md5 = "c000b315c14d38cc92914debce8bb513",
        sha1 = "4e5a306d263e99130483a6031d81a4e7c65d0b68",
        sha256 = "c5cdd27809ef14dfca38531d9770e0dd2287e29a48b9042940a7b7cebf2b1201",
        report = FileScanResponse(
            date = instant,
            status = ScanStatus.OK,
            filename = "hello.txt",
            results = listOf(
                AntivirusReportResponse(
                    antivirus = "ClamAV",
                    status = ScanStatus.OK,
                    malwareDescription = "OK",
                    virusDatabaseVersion = "1.1.1701"
                ),
                AntivirusReportResponse(
                    antivirus = "Comodo",
                    status = ScanStatus.OK,
                    malwareDescription = "is OK",
                    virusDatabaseVersion = "74656"
                )
            )
        )
    )
)