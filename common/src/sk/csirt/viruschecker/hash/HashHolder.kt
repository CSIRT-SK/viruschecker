package sk.csirt.viruschecker.hash

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.Serializable
import java.math.BigInteger
import java.security.MessageDigest

sealed class HashAlgorithm(
    private val algorithm: String,
    val hashLength: Int
) {

    suspend fun hash(file: File) =
        hash(file.inputStream().buffered())

    suspend fun hash(inputStream: InputStream): String {
        val digest = withContext(IO) {
            val digest = MessageDigest.getInstance(algorithm)
            inputStream.use { inputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var sizeRead = inputStream.read(buffer)
                while (sizeRead != -1) {
                    digest.update(buffer, 0, sizeRead)
                    sizeRead = inputStream.read(buffer)
                }
            }
            digest
        }

        val hashBytes = digest.digest()
        val hashInt = BigInteger(1, hashBytes)
        val hashText = hashInt.toString(16)
        return if (hashText.length < hashLength)
            "0".repeat(hashLength - hashText.length) + hashText
        else
            hashText
    }

    suspend fun hash(string: String): String =
        hash(string.byteInputStream())

    override fun toString(): String = algorithm

    class Sha256 : HashAlgorithm("SHA-256", 64)

    class Md5 : HashAlgorithm("MD5", 32)

    class Sha1 : HashAlgorithm("SHA-1", 40)
}

data class HashHolder(val value: String, val algorithm: String) : Serializable {
    constructor(value: String, algorithm: HashAlgorithm) : this(value, algorithm.toString())
}

suspend fun InputStream.sha256() = HashAlgorithm.Sha256().hash(this)
suspend fun InputStream.md5() = HashAlgorithm.Md5().hash(this)
suspend fun InputStream.sha1() = HashAlgorithm.Sha1().hash(this)

suspend fun File.sha256() = this.inputStream().buffered().sha256()
suspend fun File.md5() = this.inputStream().buffered().md5()
suspend fun File.sha1() = this.inputStream().buffered().sha1()

fun ByteArrayInputStream.sha256() = runBlocking { HashAlgorithm.Sha256().hash(this@sha256) }
fun ByteArrayInputStream.sha1() = runBlocking { HashAlgorithm.Sha1().hash(this@sha1) }
fun ByteArrayInputStream.md5() = runBlocking { HashAlgorithm.Md5().hash(this@md5) }

fun String.sha256() = this.byteInputStream().sha256()
fun String.sha1() = this.byteInputStream().sha1()
fun String.md5() = this.byteInputStream().md5()

fun Number.sha256() = this.toString().sha256()
fun Number.sha1() = this.toString().sha1()
fun Number.md5() = this.toString().md5()

