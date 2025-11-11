package dyno

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PolymorphismTest2 {
    private sealed class Vehicle: PolymorphicSchema(UnknownKeysStrategy.Skip) {
        val name by dynoKey<String>()

        companion object: Vehicle() {
            override fun name() = "vehicle"
        }
    }

    private object Bicycle: Vehicle() {
        override fun name() = "bicycle"
        val department by dynoKey<String>()
    }

    private object Car: Vehicle() {
        override fun name() = "car"
        val wheels by dynoKey<Int?>()
    }

    private val Car.extension get() = SchemaProperty<Car, Int>("ext", Int.serializer(), 0)
    private val Car.extension2 by dynoKey<Int>()

    @Serializable
    private data class Holder(val vehicle: @Serializable(Vehicle.Companion::class) Entity<Vehicle>)

    val json = Json {
        serializersModule = SerializersModule {
            dynoSchemaRegistry {
                polymorhic(Vehicle) {
                    schema(Bicycle)
                    schema(Car)
                }
            }
        }
    }

    @Test
    fun simple() {
        val person = Car.new {
            name set "Toyota"
            wheels set 4
            extension set 455
        }

        val encoded = json.encodeToString(Holder(person))
        val decoded = json.decodeFromString<Holder>(encoded).vehicle

        with(decoded as DynoMapImpl) {
            // check all properties are deserialized
            assertEquals("Toyota", DynoMapBase.Unsafe.data?.get(Vehicle.name))
            assertEquals(4, DynoMapBase.Unsafe.data?.get(Car.wheels))
            assertEquals(2, DynoMapBase.Unsafe.data?.size)
        }

        assertEquals("Toyota", decoded[Car.name])
        assertEquals("Toyota", decoded[Vehicle.name])
        assertEquals(Car, decoded.schema)
    }

    @Test
    fun discriminator_is_last() {
        val decoded = json.decodeFromString<Holder>(
            """{"vehicle":{"name":"XYZ","wheels":4,"type":"car"}}"""
        ).vehicle

        with(decoded as DynoMapImpl) {
            // check all properties are deserialized
            assertEquals("XYZ", DynoMapBase.Unsafe.data?.get(Vehicle.name))
            assertEquals(4, DynoMapBase.Unsafe.data?.get(Car.wheels))
            assertEquals(2, DynoMapBase.Unsafe.data?.size)
        }

        assertEquals("XYZ", decoded[Car.name])
        assertEquals(Car, decoded.schema)
    }

    @Test
    fun discriminator_is_first() {
        val decoded = json.decodeFromString<Holder>(
            """{"vehicle":{"type":"car","name":"XYZ","wheels":4}}"""
        ).vehicle

        with(decoded as DynoMapImpl) {
            // check all properties are deserialized
            assertEquals("XYZ", DynoMapBase.Unsafe.data?.get(Vehicle.name))
            assertEquals(4, DynoMapBase.Unsafe.data?.get(Car.wheels))
            assertEquals(2, DynoMapBase.Unsafe.data?.size)
        }

        assertEquals("XYZ", decoded[Car.name])
        assertEquals(Car, decoded.schema)
    }

    @Test
    fun missing_discriminator() {
        assertContains(
            assertFailsWith<SerializationException> {
                json.decodeFromString<Holder>(
                    """{"vehicle":{"name":"XYZ","wheels":4}}"""
                )
            }.message!!,
            "Field 'type' is required for type with serial name 'vehicle', but it was missing"
        )
    }

    @Test
    fun missing_property() {
        assertEquals(
            "Discriminator not found and no default schema provided for base schema 'vehicle'",
            assertFailsWith<SerializationException> {
                json.decodeFromString<Holder>(
                    """{"vehicle":{"type":"car","name":"XYZ","wheels":4}}"""
                )
            }.message
        )
    }
}