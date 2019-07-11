package sk.csirt.viruschecker.hash

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.io.InputStream

data class Hash(val value: String, val algorithm: HashAlgorithm)

sealed class HashAlgorithm(private val algorithm: String) {
    private val digest: MessageDigest = MessageDigest.getInstance(algorithm)

    fun hash(inputStream: InputStream): Hash {
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
        val hashTextPadded = if (hashText.length < 32)
            "0".repeat(32 - hashText.length) + hashText
        else
            hashText
        return Hash(hashTextPadded, this)
    }

    fun hash(file: File) = hash(file.inputStream().buffered())

    override fun toString(): String = algorithm
}

class Md5 : HashAlgorithm("Md5")

class Sha256 : HashAlgorithm("SHA-256")