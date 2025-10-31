package dyno

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

sealed class AbstractMutableDynoTest: AbstractDynoTest() {
    // add 3 keys
    // manipulate them
    // remove all one by one
    internal fun mutationCheck(m: DynamicObjectImpl) {
        val initialKeySet = m.keyNames.toSet()
        val keySet = initialKeySet.toMutableSet()

        fun checkKeys() {
            assertEquals(keySet, m.keyNames.toSet())
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

        if (r1 !in m) {
            assertEquals("r1", assertFailsWith<NoSuchDynoKeyException> { m[r1] }.key)
        }
        if (pListOfInts !in m) {
            assertEquals("listOfInts", assertFailsWith<NoSuchDynoKeyException> { m.getOrFail(pListOfInts) }.key)
        }

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