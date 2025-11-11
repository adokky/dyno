package dyno

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.*

class BigSchemaTest {
    private object BigSchema: SimpleDynoSchema("big") {
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

        val k10 by dynoKey<Int>()
        val k11 by dynoKey<Int>()
        val k12 by dynoKey<Int>()
        val k13 by dynoKey<Int>()
        val k14 by dynoKey<Int>()
        val k15 by dynoKey<Int>()
        val k16 by dynoKey<Int>()
        val k17 by dynoKey<Int>()
        val k18 by dynoKey<Int>()
        val k19 by dynoKey<Int>()

        val k20 by dynoKey<Int>()
        val k21 by dynoKey<Int>()
        val k22 by dynoKey<Int>()
        val k23 by dynoKey<Int>()
        val k24 by dynoKey<Int>()
        val k25 by dynoKey<Int>()
        val k26 by dynoKey<Int>()
        val k27 by dynoKey<Int>()
        val k28 by dynoKey<Int>()
        val k29 by dynoKey<Int>()

        val k30 by dynoKey<Int>()
        val k31 by dynoKey<Int>()
        val k32 by dynoKey<Int>()
        val k33 by dynoKey<Int>()
        val k34 by dynoKey<Int>()
        val k35 by dynoKey<Int>()
        val k36 by dynoKey<Int>()
        val k37 by dynoKey<Int>()
        val k38 by dynoKey<Int>()
        val k39 by dynoKey<Int>()
    }

    private val map = dynamicObjectOf(
        BigSchema.k0 with 0,
        BigSchema.k1 with 1,
        BigSchema.k2 with 2,
        BigSchema.k3 with 3,
        BigSchema.k4 with 4,
        BigSchema.k5 with 5,
        BigSchema.k6 with 6,
        BigSchema.k7 with 7,
        BigSchema.k8 with 8,
        BigSchema.k9 with 9,

        BigSchema.k10 with 10,
        BigSchema.k11 with 11,
        BigSchema.k12 with 12,
        BigSchema.k13 with 13,
        BigSchema.k14 with 14,
        BigSchema.k15 with 15,
        BigSchema.k16 with 16,
        BigSchema.k17 with 17,
        BigSchema.k18 with 18,
        BigSchema.k19 with 19,

        BigSchema.k20 with 20,
        BigSchema.k21 with 21,
        BigSchema.k22 with 22,
        BigSchema.k23 with 23,
        BigSchema.k24 with 24,
        BigSchema.k25 with 25,
        BigSchema.k26 with 26,
        BigSchema.k27 with 27,
        BigSchema.k28 with 28,
        BigSchema.k29 with 29,

        BigSchema.k30 with 30,
        BigSchema.k31 with 31,
        BigSchema.k32 with 32,
        BigSchema.k33 with 33,
        BigSchema.k34 with 34,
        BigSchema.k35 with 35,
        BigSchema.k36 with 36,
        BigSchema.k37 with 37,
        BigSchema.k38 with 38,
        BigSchema.k39 with 39,
    )

    @Test
    fun successful_decoding() {
        val encoded = Json.encodeToString(BigSchema, map)
        val decoded = Json.decodeFromString(BigSchema, encoded)

        assertEquals(map, decoded)
    }

    @Test
    fun successful_new() {
        val created = BigSchema.new {
            BigSchema.k0 set 0
            BigSchema.k1 set 1
            BigSchema.k2 set 2
            BigSchema.k3 set 3
            BigSchema.k4 set 4
            BigSchema.k5 set 5
            BigSchema.k6 set 6
            BigSchema.k7 set 7
            BigSchema.k8 set 8
            BigSchema.k9 set 9
            BigSchema.k10 set 10
            BigSchema.k11 set 11
            BigSchema.k12 set 12
            BigSchema.k13 set 13
            BigSchema.k14 set 14
            BigSchema.k15 set 15
            BigSchema.k16 set 16
            BigSchema.k17 set 17
            BigSchema.k18 set 18
            BigSchema.k19 set 19
            BigSchema.k20 set 20
            BigSchema.k21 set 21
            BigSchema.k22 set 22
            BigSchema.k23 set 23
            BigSchema.k24 set 24
            BigSchema.k25 set 25
            BigSchema.k26 set 26
            BigSchema.k27 set 27
            BigSchema.k28 set 28
            BigSchema.k29 set 29
            BigSchema.k30 set 30
            BigSchema.k31 set 31
            BigSchema.k32 set 32
            BigSchema.k33 set 33
            BigSchema.k34 set 34
            BigSchema.k35 set 35
            BigSchema.k36 set 36
            BigSchema.k37 set 37
            BigSchema.k38 set 38
            BigSchema.k39 set 39
        }

        assertEquals(map, created)
    }

    fun String.checkMessage(vararg allMissingExcept: Int) {
        repeat(40) { i ->
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
            BigSchema.new { }
        }.message!!.checkMessage(-1)

        assertFailsWith<IllegalStateException> {
            BigSchema.new { k39 set 42 }
        }.message!!.checkMessage(39)
    }

    @Test
    fun mising_fields_on_deserialization() {
        assertFailsWith<SerializationException> {
            Json.decodeFromString(BigSchema, """{}""")
        }.message!!.checkMessage(-1)

        assertFailsWith<SerializationException> {
            Json.decodeFromString(BigSchema, """{ "k39": 42, "k14": 111 }""")
        }.message!!.checkMessage(39, 14)
    }

    @Test
    fun error_on_unknown_field() {
        assertContains(
            assertFailsWith<SerializationException> {
                Json.decodeFromString(BigSchema, """{ "xyz": [] }""")
            }.message!!,
            "'xyz'"
        )
    }
}