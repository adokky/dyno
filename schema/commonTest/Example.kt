package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class Example {
    private object Address: EntitySchema("address") {
        val street by dynoKey<String>()
        val house by dynoKey<String>()
    }

    private object Person: EntitySchema("person") {
        val name by dynoKey<String>()
        val age by dynoKey<Int>()
        val emails by dynoKey<List<String>>("emails")
        val address by dynoKey(Address)
    }

    private typealias PersonEntity = @Serializable(Person::class) Entity<Person>

    // Create a mutable dynamic object
    private val person: PersonEntity = Person.new {
        name set "Alice"
        age set 30
        emails set listOf("alice@example.com")
        address set Address.new {
            street set "xyx"
            house set "h"
        }
    }

    @Test
    fun construction_DSL() {
        val p = Person.new {
            name set "Bob"
            age set 11
            emails set emptyList()
            address set Address.new {
                street set "xyz"
                house set "45"
            }
        }

        assertEquals("Bob", p[Person.name])
        assertEquals(11, p[Person.age])
        assertEquals("xyz", p[Person.address][Address.street])
        assertEquals("45", p[Person.address][Address.house])
    }

    @Test
    fun lazy() {
        @Serializable
        data class Persons(val data: List<DynoMap<*>>)

        val personsJson = Json.encodeToString(Persons(listOf(person)))
        val personRestored = Json.decodeFromString<Persons>(personsJson)
            .data.single()
            .unsafeCast<DynoMap<DynoKey<*>>>()

        assertEquals(person[Person.age], personRestored[Person.age])
        assertEquals(person.unsafeCast(), personRestored)
    }

    @Test
    fun eager() {
        @Serializable
        data class Persons(val data: List<PersonEntity>)

        val personsJson = Json.encodeToString(Persons(listOf(person)))
        val personRestored = Json.decodeFromString<Persons>(personsJson).data.single()

        assertEquals(person[Person.age], personRestored[Person.age])
        assertEquals(person, personRestored)
    }
}