package sk.csirt.viruschecker.gateway.persistence

import mu.KotlinLogging
import net.openhft.chronicle.map.ChronicleMap
import java.io.File
import java.io.Serializable

private val logger = KotlinLogging.logger { }

abstract class Database<K : Serializable, V : Serializable>(
    file: File,
    name: String,
    entries: Long,
    sampleKey: K,
    sampleValue: V
) : ChronicleMap<K, V> by ChronicleMap.of(sampleKey.javaClass, sampleValue.javaClass)
    .name(name)
    .constantKeySizeBySample(sampleKey)
    .averageValue(sampleValue)
    .entries(entries)
//    .valueMarshaller(valueMarshaller)
    .createOrRecoverPersistedTo(file.also { it.parentFile.mkdirs() },
        true,
        {
            logger.error {
                "Database ${file.canonicalPath} was corrupted at index ${it.segmentIndex()}. " +
                        "Automatic error recovery has been performed. "+
                        "Full message: ${it.message()};\n${it.exception()}"
            }
        }
    )
