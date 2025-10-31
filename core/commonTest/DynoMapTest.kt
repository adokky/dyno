package dyno

import kotlinx.serialization.json.Json
import kotlin.test.*

class DynoMapTest: AbstractMutableDynoTest() {
    @Test
    fun required_key() {
        val moi = DynamicObjectImpl()
        val m = moi as MutableDynoMap<DynoKey<*>>

        assertFailsWith<NoSuchDynoKeyException> { m[r1] }
        m[r1] = "123"
        val v: String = m[r1]
        assertEquals("123", v)
    }

    @Test
    fun basic_mutation() {
        val moi = DynamicObjectImpl()
        val m = moi as MutableDynoMap<DynoKey<*>>
        assertNull(m[p1])
        assertNull(m[p2])
        assertNull(m[p3])
        mutationCheck(moi)
    }

    @Test
    fun put_all() {
        val moi = DynamicObjectImpl()
        val m = moi as MutableDynoMap<DynoKey<*>>

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
        val moi = mutableDynamicObjectOf(
            p1.with("xyz"),
            p2.with(123),
            p3.with(listOf(true))
        )
        val m = moi as MutableDynoMap<DynoKey<*>>

        assertNotEquals(0, m.hashCode())
        m.clear()

        assertNull(m[p1])
        assertNull(m[p2])
        assertNull(m[p3])
        assertEquals(0, m.size)
        assertEquals(0, m.hashCode())
    }

    @Test
    fun put_entry() {
        val moi = mutableDynamicObjectOf(p1 with "old")
        val m = moi as MutableDynoMap<DynoKey<*>>
        assertEquals("old", m.put(p1 with "new"))
        assertNull(m.put(p2 with 123))
        assertEquals("new", m[p1])
        assertEquals(123, m[p2])
    }

    @Test
    fun set_entry() {
        val moi = mutableDynamicObjectOf(p1 with "old")
        val m = moi as MutableDynoMap<DynoKey<*>>
        m.set(p1 with "new")
        m.set(p2 with 123)
        assertEquals("new", m[p1])
        assertEquals(123, m[p2])
    }

    @Test
    fun decode_undefined() {
        val jsonString = """{ "${p1.name}": null }"""

        val obj = Json.decodeFromString(DynoMapSerializer, jsonString)

        assertNull(obj[p1])
        assertNull(obj[p2])

        assertFalse(p1 in obj)
        assertFalse(p2 in obj)
    }

    @Test
    fun complex_mutation() {
        val moi = DynamicObjectImpl()
        val m = moi as MutableDynoMap<DynoKey<*>>

        val initialKeySet = moi.keyNames.toSet()
        val keySet = initialKeySet.toMutableSet()

        fun checkKeys() {
            assertEquals(keySet, moi.keyNames.toSet())
        }
        fun checkKeyRemoved(key: DynoKey<*>) { keySet -= key.name; checkKeys() }
        fun checkKeyAdded(  key: DynoKey<*>) { keySet += key.name; checkKeys() }

        val mapWasEmpty = m.isEmpty() || (setOf(p1.name, p2.name, p3.name) == initialKeySet)

        m[p1] = "hello";             checkKeyAdded(p1)
        m[p2] = 42;                  checkKeyAdded(p2)
        m[p3] = listOf(true, false); checkKeyAdded(p3)

        assertTrue(p1 in m)
        assertTrue(p2 in m)
        assertTrue(p3 in m)

        assertEquals("hello", m[p1])
        assertEquals(42, m[p2])
        assertEquals(listOf(true, false), m[p3])

        assertEquals(42, m.remove(p2))
        assertNull(m.remove(p2))
        checkKeyRemoved(p2)

        assertTrue(p1 in m)
        assertFalse(p2 in m)
        assertTrue(p3 in m)

        m[p2] = 31
        assertEquals(31, m[p2])
        checkKeyAdded(p2)

        m -= p2
        assertNull(m[p2])
        checkKeyRemoved(p2)

        assertEquals("hello", m[p1])
        assertNull(m[p2])
        assertEquals(listOf(true, false), m[p3])

        assertEquals("hello", m.remove(p1))
        checkKeyRemoved(p1)

        assertEquals(listOf(true, false), m.put(p3, emptyList()))
        checkKeys()

        m[p3] = null
        checkKeyRemoved(p3)

        assertEquals(mapWasEmpty, m.isEmpty())
    }
}