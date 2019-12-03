package sk.csirt.viruschecker.gateway.persistence

import org.jetbrains.exposed.dao.LongIdTable
import sk.csirt.viruschecker.hash.HashAlgorithm
import sk.csirt.viruschecker.routing.payload.ScanStatus

object ScanReports : LongIdTable("scan_reports") {
    val sha256 = varchar("sha256", length = HashAlgorithm.Sha256().hashLength).uniqueIndex("uq_sr_sha256")
    val sha1 = varchar("sha1", length = HashAlgorithm.Sha1().hashLength).index("uq_sr_sha1")
    val md5 = varchar("md5", length = HashAlgorithm.Md5().hashLength).index("idx_sr_sha1")
    val filename = varchar("filename", length = 1024)
    val date = datetime("date_time")
}

object AntivirusReportItems: LongIdTable("antivirus_report_items"){
    val scanReport = reference("scan_report", ScanReports)
    val antivirus = varchar("antivirus", 128)
    val status = enumeration("status", ScanStatus::class)
    val malwareDescription = varchar("malware_description", 128)
    val virusDatabaseVersion = varchar("virus_database_version", 64)
}