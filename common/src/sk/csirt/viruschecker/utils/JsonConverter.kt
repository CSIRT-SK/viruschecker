package sk.csirt.viruschecker.utils

import com.google.gson.GsonBuilder

object JsonConverter {
    val implementation = GsonBuilder().setPrettyPrinting().create()

    fun <T> toJson(something: T): String =
        implementation.toJson(something)

    inline fun <reified T> fromJson(json: String): T =
        implementation.fromJson(json, T::class.java)

}

fun <T> T.json(): String = JsonConverter.toJson(this)

inline fun <reified T> String.fromJson(): T = JsonConverter.fromJson(this)