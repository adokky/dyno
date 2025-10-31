package dyno

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTests: AbstractMutableDynoTest() {
    @Test
    fun serialization_lazy() {
        val m = DynamicObjectImpl()

        assertEquals(0, m.size)

        m[p1] = "foo"
        m[p2] = 42
        m[p3] = listOf(true, false)

        assertEquals(3, m.size)

        val actual = m.encodeDecode()

        assertEquals(m, actual)
        assertEquals(actual, m)

        mutationCheck(actual)
    }

    @Test
    fun serialization_empty() {
        val m = DynamicObjectImpl()

        val actual = m.encodeDecode()

        assertEquals(m, actual)
        assertEquals(actual, m)

        mutationCheck(actual)
    }

    @Test
    fun serialization_nulls() {
        val decoded = Json.decodeFromString(DynamicObjectSerializer, """{"p1":"xyz","p2":null,"p3":null}""")
        val expected = buildDynamicObject { p1 set "xyz" }
        assertEquals(expected, decoded)
    }

    @Test
    fun remove_undecoded_key() {
        val m = dynamicObjectOf(p2 with 123, p3 with listOf(false)).encodeDecode()
        m -= p2
        assertEquals(listOf(false), m.remove(p3))
    }

    @Test
    fun remove_decoded_key() {
        val m = dynamicObjectOf(p2 with 123, p3 with listOf(false)).encodeDecode()
        assertEquals(123, m[p2])
        assertEquals(listOf(false), m[p3])
        m -= p2
        assertEquals(listOf(false), m.remove(p3))
    }

    @Test
    fun key_list_of_partially_decoded_map() {
        val m = dynamicObjectOf(
            p1 with "decoded",
            p3 with listOf(false)
        ).encodeDecode()

        assertEquals("decoded", m[p1])

        assertEquals(setOf("p1", "p3"), m.keyNames.toSet())
    }
}