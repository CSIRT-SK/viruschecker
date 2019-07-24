package sk.csirt.viruschecker.gateway.config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.config.defaultTimeout
import java.io.File
import java.nio.charset.Charset
import java.time.Duration

data class Drivers(val internal: List<String>, val external: List<String>) {

    fun get(useAlsoExternal: Boolean): Iterable<String> = object : Iterable<String> {
        override fun iterator(): Iterator<String> {
            return iterator(useAlsoExternal)
        }
    }

    private fun iterator(useAlsoExternal: Boolean): Iterator<String> = object : Iterator<String> {
        private val internalIterator = internal.iterator()
        private val externalIterator = external.iterator()

        override fun hasNext(): Boolean = if (useAlsoExternal)
            internalIterator.hasNext() || externalIterator.hasNext()
        else
            internalIterator.hasNext()

        override fun next(): String {
            return if (useAlsoExternal) {
                if (internalIterator.hasNext()) internalIterator.next()
                else externalIterator.next()
            } else {
                internalIterator.next()
            }
        }

    }
}

class CommandLineArguments(parser: ArgParser) {
    val drivers by parser.positional(
        help = "List of urls containing virus checker drivers."
    ) {
        FileUtils.readLines(File(this), Charset.defaultCharset())
            .map { it.trim() }
            .filterNot { it.startsWith("#") }
            .partition { it.startsWith("ext:") }
            .let {
                Drivers(
                    internal = it.second.map { it },
                    external = it.first.map { it.replace("ext:", "") })
            }
    }

    val socketTimeout: Duration by parser.storing(
        "-t", "--timeout",
        help = "Optional: Set socket timeout in milliseconds. Default is value " +
                "${defaultTimeout.toMillis()}."
    ) { Duration.ofMillis(this.toLong()) }.default(
        defaultTimeout - Duration.ofMillis((defaultTimeout.toMillis() * 0.75).toLong())
    )
}