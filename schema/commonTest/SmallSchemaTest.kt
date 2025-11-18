package dyno

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.*

class SmallSchemaTest {
    private object SmallSchema: SimpleDynoSchema("small") {
        val k0 by dynoKey<Int>()
        val k1 by dynoKey<Int>()
        val k2 by dynoKey<Int>()
        val k3 by dynoKey<Int>()
        val k4 by dynoKey<Int>()
        val k5 by dynoKey<Int>()
        val k6 by dynoKey<Int>()
        val k7 by dynoKey<Int>()
        val k8 by dynoKey<Int>()
        val k9 by dynoKey<Int>()
    }

    private val map = dynamicObjectOf(
        SmallSchema.k0 with 0,
        SmallSchema.k1 with 1,
        SmallSchema.k2 with 2,
        SmallSchema.k3 with 3,
        SmallSchema.k4 with 4,
        SmallSchema.k5 with 5,
        SmallSchema.k6 with 6,
        SmallSchema.k7 with 7,
        SmallSchema.k8 with 8,
        SmallSchema.k9 with 9,
    )

    @Test
    fun successful_decoding() {
        val encoded = Json.encodeToString(SmallSchema, map)
        val decoded = Json.decodeFromString(SmallSchema, encoded)

        assertEquals(map, decoded)
    }

    @Test
    fun successful_new() {
        val created = SmallSchema.new {
            SmallSchema.k0 set 0
            SmallSchema.k1 set 1
            SmallSchema.k2 set 2
            SmallSchema.k3 set 3
            SmallSchema.k4 set 4
            SmallSchema.k5 set 5
            SmallSchema.k6 set 6
            SmallSchema.k7 set 7
            SmallSchema.k8 set 8
            SmallSchema.k9 set 9
        }

        assertEquals(map, created)
    }

    fun String.checkMessage(vararg allMissingExcept: Int) {
        repeat(9) { i ->
            val fragment = "k$i"
            if (i in allMissingExcept) {
                assertFalse(fragment in this)
            } else {
                assertContains(this, fragment)
            }
        }
    }

    @Test
    fun mising_fields_on_new() {
        assertFailsWith<IllegalStateException> {
            SmallSchema.new { }
        }.message!!.checkMessage(-1)

        assertFailsWith<IllegalStateException> {
            SmallSchema.new { k7 set 42 }
        }.message!!.checkMessage(7)
    }

    @Test
    fun mising_fields_on_deserialization() {
        assertFailsWith<SerializationException> {
            Json.decodeFromString(SmallSchema, """{}""")
        }.message!!.checkMessage(-1)

        assertFailsWith<SerializationException> {
            Json.decodeFromString(SmallSchema, """{ "k6": 42, "k1": 111 }""")
        }.message!!.checkMessage(1, 6)
    }

    @Test
    fun error_on_unknown_field() {
        assertContains(
            assertFailsWith<SerializationException> {
                Json.decodeFromString(SmallSchema, """{ "xyz": [] }""")
            }.message!!,
            "'xyz'"
        )
    }
}