package dyno

import dyno.Example.Person
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

typealias SerEntity<S> = Entity<out @Serializable(DynoSchemaSerializer::class) TypedDynoSchema<S>>

typealias PersonEntity = @Serializable(Person::class) Entity<Person>

@Serializable(TestSer::class)
class TestClass<T>

object TestSer: KSerializer<TestClass<*>> {
    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor
    override fun serialize(encoder: Encoder, value: TestClass<*>) {
        encoder.encodeString("hi")
    }
    override fun deserialize(decoder: Decoder): TestClass<*> = TestClass<Int>()
}

@Serializable
class TestHolder(val tc: TestClass<@Contextual Any>)

class Example {
    object Address: TypedDynoSchema<Address>("address") {
        val street by key<String>()
        val house by key<String>()
    }

    object Person: TypedDynoSchema<Person>("person") {
        val t = test()
        val name by key<String>()
        val age by key<Int>()
        val emails = key<List<String>>("emails")
        val address by key(Address)
    }

    // Create a mutable dynamic object
    val person: PersonEntity = Person.new {
        name set "Alice"
        age set 30
        emails set listOf("alice@example.com")
        address set Address.new {
            street set "xyx"
        }
    }

    @Test
    fun ktxCheck() {
        println(Json.encodeToString(TestHolder(TestClass())))
    }

    @Test
    fun constructionDsl() {
        val p = Person.new {
            name set "Bob"
            age set 11
        }

        assertEquals("Bob", p[Person.name])
        assertEquals(11, p[Person.age])

        val addr = Address.new {
            street set "zzzz"
        }

        assertEquals("zzzz", addr[Address.street])
    }

    @Test
    fun lazy() {
        // No need to specify `DynoSerializer` when `DynamicObject` used in property type
        @Serializable
        data class Persons(val data: List<DynoMapBase>)

        val personsJson = Json.encodeToString(Persons(listOf(person)))
        val personsRestored = Json.decodeFromString<Persons>(personsJson)
        println(personsRestored)
    }

    @Test
    fun eager() {
        // Every `DynoSchema` implements `KSerializer`,
        @Serializable
        data class Persons(val data: List<PersonEntity>)

        val personsJson = Json.encodeToString(Persons(listOf(person)))
        val personsRestored = Json.decodeFromString<Persons>(personsJson)
        println(personsRestored)
    }
}