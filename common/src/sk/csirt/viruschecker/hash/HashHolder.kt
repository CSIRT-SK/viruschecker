package sk.csirt.viruschecker.hash

import java.io.File
import java.io.InputStream
import java.io.Serializable
import java.math.BigInteger
import java.security.MessageDigest

sealed class HashAlgorithm(
    private val algorithm: String,
    private val hashLength: Int
) {
    private val digest: MessageDigest = MessageDigest.getInstance(algorithm)

    open fun hash(inputStream: InputStream): HashHolder {

        inputStream.use {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var sizeRead = inputStream.read(buffer)
            while (sizeRead != -1) {
                digest.update(buffer, 0, sizeRead)
                sizeRead = inputStream.read(buffer)
            }
        }

        val hashBytes = digest.digest()
        val hashInt = BigInteger(1, hashBytes)
        val hashText = hashInt.toString(16)
        val hashTextPadded = if (hashText.length < hashLength)
            "0".repeat(hashLength - hashText.length) + hashText
        else
            hashText
       return HashHolder(hashTextPadded, algorithm)
    }

    class Sha256 : HashAlgorithm("SHA-256", 64)

    class Md5: HashAlgorithm("MD5", 32)
}

data class HashHolder(val value: String, val algorithm: String): Serializable

fun InputStream.sha256(): HashHolder = HashAlgorithm.Sha256().hash(this)
fun InputStream.md5(): HashHolder = HashAlgorithm.Md5().hash(this)

fun File.sha256(): HashHolder = this.inputStream().buffered().sha256()
fun File.md5(): HashHolder = this.inputStream().buffered().md5()
