package sk.csirt.viruschecker.driver.antivirus

interface ExternalAntivirus : Antivirus {
    override suspend fun scanFile(params: FileScanParameters): FileScanResult =
        if (params.externalServicesAllowed) {
            externalScanFile(params)
        } else {
            FileScanResult(
                filename = params.originalFileName,
                scanReport = ScanResult(
                    status = ScanStatusResult.SCAN_REFUSED,
                    reports = listOf(
                        AntivirusReportResult(
                            antivirusName = antivirusName,
                            status = ScanStatusResult.SCAN_REFUSED,
                            malwareDescription = "The caller did not want to use this external service.",
                            virusDatabaseVersion = ""
                        )
                    )
                )
            )
        }

    suspend fun externalScanFile(params: FileScanParameters): FileScanResult
}