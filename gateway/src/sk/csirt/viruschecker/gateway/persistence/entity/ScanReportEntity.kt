package sk.csirt.viruschecker.gateway.persistence.entity

import java.io.Serializable
import java.time.Instant

data class ScanReportEntity(
    val sha256: String,
    val sha1: String,
    val md5: String,
    val filename: String,
    val date: Instant,
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
//        val results = toWrite.results
//        for (ir in 0..results.size - 2) {
//            sb.append(results[ir].antivirus).append("; ")
//            sb.append(results[ir].status).append("; ")
//            sb.append(results[ir].malwareDescription).append(", ")
//
//        }
//        val ir = results.size - 1
//        sb.append(results[ir].antivirus).append("; ")
//        sb.append(results[ir].status).append("; ")
//        sb.append(results[ir].malwareDescription).append("]")
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