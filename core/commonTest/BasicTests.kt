package dyno

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.test.*

open class BasicTests: AbstractMutableDynoTest() {
    @Test
    fun required_key() {
        val m = DynamicObjectImpl()
        assertFailsWith<NoSuchDynoKeyException> { m[r1] }
        m[r1] = "123"
        val v: String = m[r1]
        assertEquals("123", v)
    }

    @Test
    fun is_empty() {
        val m = mutableDynamicObjectOf()

        fun assertEmpty(value: Boolean) {
            if (value) assertEquals(0, m.size)
            assertEquals(value, m.isEmpty())
            assertEquals(value, m.isNullOrEmpty())
            assertNotEquals(value, m.isNotEmpty())
        }

        assertEmpty(true)
        m[r2] = 123
        assertEmpty(false)
        m -= r2
        assertEmpty(true)

        assertTrue((null as DynoMapBase?).isNullOrEmpty())
    }

    @Test
    fun dynamic_object_of() {
        for (m in listOf(
            dynamicObjectOf(p2 with 42),
            dynamicObjectOf(listOf(p2 with 42))
        )) {
            assertEquals(42, m[p2])
            assertEquals(1, m.size)
        }

        assertTrue(dynamicObjectOf().isEmpty())
    }

    @Test
    fun basic_mutation() {
        val m = DynamicObjectImpl()
        assertNull(m[p1])
        assertNull(m[p2])
        assertNull(m[p3])
        mutationCheck(m)
    }

    @Test
    fun put_entry() {
        val m = mutableDynamicObjectOf(listOf(p1 with "old"))
        assertEquals("old", m.put(p1 with "new"))
        assertNull(m.put(p2 with 123))
        assertEquals("new", m[p1])
        assertEquals(123, m[p2])
    }

    @Test
    fun set_entry() {
        val m = mutableDynamicObjectOf(listOf(p1 with "old"))
        m.set(p1 with "new")
        m.set(p2 with 123)
        assertEquals("new", m[p1])
        assertEquals(123, m[p2])
    }

    @Test
    fun put_all() {
        val m = DynamicObjectImpl()

        m.putAll(
            listOf(
                p2.with(-987),
                p3.with(listOf(true)),
            )
        )

        assertEquals(-987, m[p2])
        assertEquals(listOf(true), m[p3])
    }

    @Test
    fun clear_object() {
        val m = mutableDynamicObjectOf(
            p1.with("xyz"),
            p2.with(123),
            p3.with(listOf(true))
        )

        assertNotEquals(0, m.hashCode())
        m.clear()

        assertNull(m[p1])
        assertNull(m[p2])
        assertNull(m[p3])
        assertEquals(0, m.size)
        assertEquals(0, m.hashCode())
    }

    @Test
    fun decodeUndefined() {
        val jsonString = """{ "${p1.name}": null }"""

        val obj = Json.decodeFromString(DynamicObjectSerializer, jsonString)

        assertNull(obj[p1])
        assertNull(obj[p2])

        assertFalse(p1 in obj)
        assertFalse(p2 in obj)
    }

    @Test
    fun simple_contains() {
        val m = dynamicObjectOf(e1, e2).encodeDecode()
        m.getOrFail(e2.key) // decode key

        assertTrue(e1.key in m)
        assertTrue(e2.key in m)
        assertFalse(e3.key in m)
    }

    @Test
    fun contains_string() {
        val m = dynamicObjectOf(e1, e2).encodeDecode()
        m.getOrFail(e2.key) // decode key

        assertTrue(e1.key.name in m)
        assertTrue(e2.key.name in m)
        assertFalse(e3.key.name in m)
    }

    @Test
    fun remove_and_get() {
        val m = dynamicObjectOf(e1, e2).encodeDecode()
        m.getOrFail(e2.key) // decode key

        with(m) {
            assertEquals(e2.value, DynoMapBase.Unsafe.removeAndGet(e2.key))
            assertNull(m[e2.key])
            assertEquals(e1.value, DynoMapBase.Unsafe.removeAndGet(e1.key))
            assertNull(m[e1.key])
            assertNull(DynoMapBase.Unsafe.removeAndGet(e1.key))
        }

        assertTrue(m.isEmpty())
    }

    @Test
    fun remove_key() {
        val m = dynamicObjectOf(e1, e2).encodeDecode()
        m.getOrFail(e2.key) // decode key

        with(m) {
            assertTrue(DynoMapBase.Unsafe.remove(e2.key))
            assertNull(m[e2.key])
            assertTrue(DynoMapBase.Unsafe.remove(e1.key))
            assertNull(m[e1.key])
            assertFalse(DynoMapBase.Unsafe.remove(e1.key))
        }

        assertTrue(m.isEmpty())
    }

    @Test
    fun remove_string_key() {
        val m = dynamicObjectOf(e1, e2).encodeDecode()
        m.getOrFail(e2.key) // decode key
        with(m) {
            assertTrue(DynoMapBase.Unsafe.remove(e2.key.name))
            assertNull(m[e2.key])
            assertTrue(DynoMapBase.Unsafe.remove(e1.key.name))
            assertNull(m[e1.key])
            assertFalse(DynoMapBase.Unsafe.remove(e1.key.name))
        }
        assertTrue(m.isEmpty())
    }

    @Test
    fun get_stateless() {
        val m = dynamicObjectOf(e1, e2, e3).encodeDecode()
        with(m) {
            repeat(3) {
                assertEquals(e2.value, DynoMapBase.Unsafe.getStateless(e2.key))
                assertEquals(e1.value, DynoMapBase.Unsafe.getStateless(e1.key))
            }
            assertEquals(e1.value, m[e1.key])
            assertEquals(e2.value, m[e2.key])
            assertEquals(e3.value, m[e3.key])
            repeat(3) {
                assertEquals(e3.value, DynoMapBase.Unsafe.getStateless(e3.key))
            }
            assertNull(DynoMapBase.Unsafe.getStateless(r1))
        }
    }

    @Test
    fun operations_on_empty() {
        fun check(m: MutableDynamicObject) {
            for (doClear in booleanArrayOf(false, true)) {
                if (doClear) m.clear()

                assertFalse(p1 in m)
                assertFalse(p1.name in m)
                m -= p1
                assertNull(m.remove(p1))
                assertEquals(dynamicObjectOf(e1), m + e1)
                assertEquals(dynamicObjectOf(), m - e1.key)
                with(m) {
                    assertNull(DynoMapBase.Unsafe.getStateless(e1.key))
                    assertFalse(DynoMapBase.Unsafe.remove(e1.key))
                    assertFalse(DynoMapBase.Unsafe.remove(e1.key.name))
                }

                assertTrue(m.isEmpty())
            }
        }

        check(mutableDynamicObjectOf())
        check(mutableDynamicObjectOf().encodeDecode())
        check(buildMutableDynamicObject { put(e1); remove(e1.key) })
        check(buildMutableDynamicObject { put(e1); remove(e1.key) }.encodeDecode())
    }

    @Test
    fun class_keys_mutation() {
        @Serializable data class Data1(val s: String)
        @Serializable data class Data2(val i: Int)

        val expected1 = Data1("xyz")
        val expected2 = Data2(456)

        val dyno = mutableDynamicObjectOf()
        assertNull(dyno.putInstance(Data1("abc")))
        dyno.setInstance(expected1)
        assertNull(dyno.putInstance(Data2(123)))
        dyno.setInstance(expected2)

        assertEquals(expected1, dyno.getInstance())
        assertEquals(expected2, dyno.getInstance())

        assertEquals(expected1, dyno.getInstanceOrFail())
        assertEquals(expected2, dyno.getInstanceOrFail())

        assertEquals(
            dynamicObjectOf(
                DynoTypeKey<Data1>() with expected1,
                DynoTypeKey<Data2>() with expected2,
            ),
            dyno
        )

        assertEquals(expected1, dyno.putInstance(Data1("overwrite")))
        assertEquals(expected2, dyno.putInstance(Data2(42)))

        val ov1 = Data1("overwrite2")
        val ov2 = Data2(94725)
        dyno.setInstance(ov1)
        assertEquals(ov1, dyno.getInstance())
        dyno.setInstance(ov2)
        assertEquals(ov2, dyno.getInstance())

        assertEquals(ov1, dyno.removeInstance())
        assertNull(dyno.getInstance<Data1>())
        assertEquals(ov2, dyno.getInstance())
        assertEquals(ov2, dyno.removeInstance())
        assertNull(dyno.getInstance<Data2>())

        assertTrue(dyno.isEmpty())

        assertFailsWith<NoSuchDynoKeyException> { dyno.getInstanceOrFail<Data1>() }
        assertFailsWith<NoSuchDynoKeyException> { dyno.getInstanceOrFail<Data2>() }
    }

    @Test
    fun class_keys_serial_name() {
        @SerialName("d1") @Serializable data class Data1(val s: String)
        @SerialName("d2") @Serializable data class Data2(val i: Int)

        val data = dynamicObjectOf(
            DynoTypeKey<Data1>() with Data1("ABC"),
            DynoTypeKey<Data2>() with Data2(321),
            DynoTypeKey<Int>() with 42
        )

        assertEquals(data, data.encodeDecode())
        val actual = Json.encodeToJsonElement(data)
        val expected = buildJsonObject {
            putJsonObject("d1") { put("s", "ABC") }
            putJsonObject("d2") { put("i", 321) }
            put("kotlin.Int", 42)
        }
        assertEquals(expected, actual)
    }
}

