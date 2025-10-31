package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EagerSerializerTest: AbstractMutableDynoTest() {
    @Test
    fun all_known() {
        val m = DynamicObjectImpl()

        m[SimpleSchema.name] = "foo"
        m[SimpleSchema.age] = 111
        m[SimpleSchema.tags] = listOf("tag1", "tag2")

        assertEquals(m, m.encodeDecode(SimpleSchema))
    }

    @Test
    fun context() {
        val ser = object : SimpleSchemaSerializer() {
            override fun resolve(context: ResolveContext): ResolveResult {
                when (context.keyString) {
                    "p1" -> {
                        assertFalse(context.isScannedUnknown("unknown1"))
                        assertNull(context.getScannedUnknown("unknown1"))
                        assertEquals(JsonPrimitive("bar"), context.getJsonValue())
                        assertEquals("bar", context.decodeValue<String>())
                    }
                    "p2" -> {
                        repeat(3) {
                            assertEquals(222, context.decodeValue(p2.serializer))
                            assertEquals(JsonPrimitive(222), context.getJsonValue())
                        }
                        assertEquals(JsonPrimitive(333), context.getScannedUnknown("r2"))
                        return ResolveResult.Keep
                    }
                    "p3" -> {
                        assertTrue(context.isScannedUnknown("unknown1"))
                        assertEquals(JsonPrimitive("zzz"), context.getScannedUnknown("unknown1"))
                        assertEquals(JsonNull, context.getJsonValue())
                        assertEquals(null, context.decodeValue(p3.serializer))
                    }
                    "r2" -> return ResolveResult.Delay
                    "unknown1" -> return ResolveResult.Keep
                }
                return super.resolve(context)
            }

            override fun postResolve(context: ResolveContext): ResolveResult {
                assertTrue(context.isScannedUnknown("unknown1"))
                assertEquals(JsonPrimitive("zzz"), context.getScannedUnknown("unknown1"))
                assertEquals(111, context.getDecoded(SimpleSchema.age))
                return if (context.keyString == "r2") r2 else ResolveResult.Skip
            }

            override fun defaultResolveResult(key: String): ResolveResult = ResolveResult.Skip
        }

        fun checkDecoded(decoded: DynamicObject) {
            val expected = buildDynamicObject {
                SimpleSchema.name set "foo"
                SimpleSchema.age set  111
                SimpleSchema.tags set listOf("tag1", "tag2")
                p2 set 222
                r2 set 333
                DynoKey<String>("unknown1") set "zzz"
            }

            assertEquals(expected, decoded)
        }

        val jsonString = """
            {
                "name": "foo",
                "tags": ["tag1", "tag2"],
                "age": 111,
                "p1": "bar",
                "unknown1": "zzz",
                "r2": 333,
                "p3": null,
                "p2": 222,
                "unknown2": [1, "", false, {}]
            }
        """.trimIndent()

        checkDecoded(Json.decodeFromString(ser, jsonString))
        checkDecoded(Json.decodeFromJsonElement(ser, Json.parseToJsonElement(jsonString)))
    }

    @Test
    fun keep_unknown() {
        val m = DynamicObjectImpl()

        m[SimpleSchema.name] = "foo"
        m[SimpleSchema.age] = 111
        m[p3] = listOf(true, false)
        m[pMapOfInts] = mapOf(1 to 2, 3 to 4)

        fun check(actual: DynamicObjectImpl) {
            assertEquals(m, actual)
            assertEquals(actual, m)

            mutationCheck(actual)
        }

        check(m.encodeDecode(SimpleSchema))

        check(Json.decodeFromString(SimpleSchema, """
            {
                "name":"foo",
                "tags": null,
                "age":111,
                "p2": null,
                "p3": [true, false],
                "mapOfInts": {"1": 2, "3": 4}
            }
        """.trimIndent()).unsafeCast())
    }

    @Test
    fun skip_unknown() {
        val ser = object :SimpleSchemaSerializer() {
            override fun defaultResolveResult(key: String): ResolveResult = ResolveResult.Skip
        }

        val m = DynamicObjectImpl()

        m[ser.name] = "bar"
        m[ser.age] = -5654
        m[p3] = listOf(true, false)
        m[pMapOfInts] = mapOf(1 to 2, 3 to 4)

        fun check(actual: DynamicObjectImpl) {
            assertEquals("bar", actual[ser.name])
            assertEquals(-5654, actual[ser.age])
            assertNull(actual[p3])
            assertNull(actual[pMapOfInts])

            mutationCheck(actual)
        }

        check(m.encodeDecode(ser))

        check(Json.decodeFromString(ser, """
            {
                "name":"bar",
                "tags": null,
                "age": -5654,
                "p2": null,
                "p3": [true, false],
                "mapOfInts": {"1": 2, "3": 4}
            }
        """.trimIndent()).unsafeCast())
    }

    @Test
    fun delay_unknown() {
        val ser = object : SimpleSchemaSerializer() {
            override fun defaultResolveResult(key: String): ResolveResult = ResolveResult.Delay

            override fun postResolve(context: ResolveContext): ResolveResult {
                return when (context.keyString) {
                    pMapOfInts.name -> ResolveResult.Keep
                    p3.name -> ResolveResult.Delay
                    else -> super.postResolve(context)
                }
            }
        }

        val m = buildDynamicObject {
            ser.name set "a string"
            ser.age set 1147
            p1 set "zzz"
            p2 set 123
            p3 set listOf(true, false)
            pMapOfInts set mapOf(1 to 2, 3 to 4)
        }

        val actual = m.encodeDecode(ser)

        assertEquals("a string", actual[ser.name])
        assertEquals(1147, actual[ser.age])
        assertEquals(m[p3], actual[p3])
        assertEquals(m[pMapOfInts], actual[pMapOfInts])
        assertNull(actual[p1])
        assertNull(actual[p2])

        mutationCheck(actual)
    }
}