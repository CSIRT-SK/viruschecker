package sk.csirt.viruschecker.utils

import com.google.gson.GsonBuilder

class JsonConverter {
    val implementation = GsonBuilder().setPrettyPrinting().create()

    fun <T> toJson(something: T): String =
        implementation.toJson(something)

    inline fun <reified T> fromJson(json: String): T =
        implementation.fromJson(json, T::class.java)

}