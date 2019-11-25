package sk.csirt.viruschecker.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class JsonConverterTest {

    data class Car(
        val id: Long,
        val model: String
    )

    data class Person(
        val id: Long,
        val name: String,
        val cars: List<Car>
    )


    private val person = Person(
        id = 1,
        name = "Ďuri Traktorista",
        cars = listOf(
            Car(
                id = 1,
                model = "KITT"
            ),
            Car(
                id = 2,
                model = "DeLorean DMC-12"
            )
        )
    )


    @Test
    fun `Test convert json`() {
        val person = Person(
            id = 1,
            name = "Ďuri Traktorista",
            cars = listOf(
                Car(
                    id = 1,
                    model = "KITT"
                ),
                Car(
                    id = 2,
                    model = "DeLorean DMC-12"
                )
            )
        )
        val json = person.json().also { println(it) }
        val personFromJson = json.fromJson<Person>()
        assertEquals(person, personFromJson)
    }

//    @Test
//    fun `Test convert invalid json`() {
//        val json = """{
//"id": 1,
//  "cars": [
//    {
//      "id": 1,
//      "model": "KITT"
//    },
//    {
//      "id": 2,
//      "model": "DeLorean DMC-12"
//    }
//  ]
//}""".trimIndent()
//        assertFails {
//            json.fromJson<Person>()
//        }
//    }
//
//    @Test
//    fun `Test convert invalid json 2`() {
//        val json = """{
//  "name": "Ďuri Traktorista",
//  "cars": [
//    {
//      "id": 1,
//      "model": "KITT"
//    },
//    {
//      "id": 2,
//      "model": "DeLorean DMC-12"
//    }
//  ]
//}""".trimIndent()
//        val pe = json.fromJson<Person>()
//        print(pe.name)
//        assertFails {
//            json.fromJson<Person>()
//        }
//    }
//
//
//    @Test
//    fun `Test convert invalid json 3`() {
//        val json = """{
//  "id": 1,
//  "name": "Ďuri Traktorista",
//  "cars": null
//}""".trimIndent()
//        assertFails {
//            json.fromJson<Person>()
//        }
//    }
}