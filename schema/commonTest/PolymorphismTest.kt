package dyno

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PolymorphismTest {
    @Suppress("UnusedReceiverParameter")
    private val Car.extension get() = SchemaProperty<Car, Int>("ext", Int.serializer(), 0)

    private typealias VehicleEntity = @Serializable(Vehicle.Companion::class) Entity<Vehicle>

    @Serializable
    private data class Holder(val vehicle: VehicleEntity)

    private val json = Json {
        serializersModule = SerializersModule {
            dynoSchemaRegistry {
                polymorhic(Vehicle) {
                    schema(Bicycle)
                    schema(Car)
                }
            }
        }
    }

    private val keepUcJson = Json(json) {
        ignoreUnknownKeys = true
    }

    @Test
    fun error_on_unknown_key() {
        val person = Car.new {
            name set "Name"
            wheels set 4
            extension set 455
        }

        val encoded = json.encodeToString(Holder(person))
        val msg = assertFailsWith<SerializationException> {
            json.decodeFromString<Holder>(encoded)
        }.message
        assertNotNull(msg)
        assertContains(msg, "'ext'")
    }

    @Test
    fun keep_unknown_key() {
        val person = Car.new {
            name set "Toyota"
            wheels set 4
            extension set 455
        }

        val encoded = keepUcJson.encodeToString(Holder(person))
        val decoded = keepUcJson.decodeFromString<Holder>(encoded).vehicle

        with(decoded as DynoMapImpl) {
            assertEquals(3, DynoMapBase.Unsafe.data?.size)
            // check all non-extension properties are deserialized
            assertEquals("Toyota", DynoMapBase.Unsafe.data?.get(Car.name))
            assertEquals(4, DynoMapBase.Unsafe.data?.get(Car.wheels))
            // extension property should stay encoded
            assertEquals(JsonPrimitive(455), DynoMapBase.Unsafe.data?.get("ext"))
            assertNull(DynoMapBase.Unsafe.data?.get(Car.extension))
        }
    }

    @Test
    fun discriminator_is_last() {
        val decoded = json.decodeFromString<Holder>(
            """{"vehicle":{"name":"XYZ","wheels":4,"type":"car"}}"""
        ).vehicle

        with(decoded as DynoMapImpl) {
            // check all properties are deserialized
            assertEquals("XYZ", DynoMapBase.Unsafe.data?.get(Car.name))
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
            assertEquals("XYZ", DynoMapBase.Unsafe.data?.get(Car.name))
            assertEquals(4, DynoMapBase.Unsafe.data?.get(Car.wheels))
            assertEquals(2, DynoMapBase.Unsafe.data?.size)
        }

        assertEquals("XYZ", decoded[Car.name])
        assertEquals(Car, decoded.schema)
    }

    @Test
    fun missing_discriminator() {
        val expectedMessage = "Field 'type' is required for type with serial name 'vehicle', but it was missing"

        asssertFailsWithMessage<SerializationException>(expectedMessage) {
            json.decodeFromString<Holder>(
                """{"vehicle":{"name":"XYZ","wheels":4}}"""
            )
        }

        asssertFailsWithMessage<SerializationException>(expectedMessage) {
            json.decodeFromString<Holder>(
                """{"vehicle":{}}"""
            )
        }
    }

    @Test
    fun missing_property() {
        asssertFailsWithMessage<SerializationException>("'name'", "'car'") {
            json.decodeFromString<Holder>(
                """{"vehicle":{"type":"car","wheels":5}}"""
            )
        }
    }

    @Test
    fun unknown_schema() {
        asssertFailsWithMessage<SerializationException>("'xyz'", "schema") {
            json.decodeFromString<Holder>(
                """{"vehicle":{"type":"xyz","wheels":5}}"""
            )
        }
    }

    @Test
    fun fallback_schema() {
        val json = Json {
            serializersModule = SerializersModule {
                dynoSchemaRegistry {
                    polymorhic(Vehicle) {
                        schema(Bicycle)
                        schema(Car)
                        fallback = Car
                    }
                }
            }
        }

        assertEquals(
            Bicycle.new { name set "123"; electric set true },
            json.decodeFromString<Holder>(
                """{"vehicle":{"type":"bicycle","electric":"true","name":"123"}}"""
            ).vehicle
        )

        assertEquals(
            Car.new { name set "CAR"; wheels set 4 },
            json.decodeFromString<Holder>(
                """{"vehicle":{"wheels":4,"name":"CAR"}}"""
            ).vehicle
        )
    }
}

