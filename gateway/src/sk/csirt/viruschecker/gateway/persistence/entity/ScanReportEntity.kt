package sk.csirt.viruschecker.gateway.persistence.entity

import sk.csirt.viruschecker.hash.HashHolder
import java.io.Serializable
import java.time.Instant

data class ScanReportEntity(
    val sha256: String,
    val filename: String,
    val date: Instant,
    val otherHashes: List<HashHolder>,
    val reports: List<AntivirusReportEntity>
): Serializable

data class AntivirusReportEntity(
    val antivirus: String,
    val status: String,
    val malwareDescription: String
): Serializable

//object ScanReportEntityBytesMarshaller :
//    Marshaller<ScanReportEntity> {
//
//    private val writer = CharSequenceBytesWriter()
//
//    override fun write(out: Bytes<*>, toWrite: ScanReportEntity) {
//
//        val sb = StringBuilder()
//            .append(toWrite.date).append("; ")
//            .append(toWrite.filename).append("; ")
//            .append(toWrite.sha256).append("; ")
//
//        sb.append("[")
//        val hashes = toWrite.otherHashes
//        for (ih in 0..hashes.size - 2) {
//            sb.append(hashes[ih].value).append("; ")
//            sb.append(hashes[ih].algorithm).append(", ")
//        }
//        val ih = hashes.size - 1
//        sb.append(hashes[ih].value).append("; ")
//        sb.append(hashes[ih].algorithm).append("]; ")
//
//        sb.append("[")
//        val reports = toWrite.reports
//        for (ir in 0..reports.size - 2) {
//            sb.append(reports[ir].antivirus).append("; ")
//            sb.append(reports[ir].status).append("; ")
//            sb.append(reports[ir].malwareDescription).append(", ")
//
//        }
//        val ir = reports.size - 1
//        sb.append(reports[ir].antivirus).append("; ")
//        sb.append(reports[ir].status).append("; ")
//        sb.append(reports[ir].malwareDescription).append("]")
//
//        out.writeUtf8(sb.toString())
//    }
//
//    override fun read(inBytes: Bytes<*>, using: ScanReportEntity?): ScanReportEntity {
//        val string = inBytes.readUtf8()
//    }
//
//    override fun readResolve(): ScanReportEntityBytesMarshaller = this
//
//}