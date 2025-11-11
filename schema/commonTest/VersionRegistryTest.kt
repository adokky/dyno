package dyno

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionRegistryTest {
    @Test
    fun try_put() {
        val registry = VersionedRegistry<Int>()
        assertTrue(registry.tryPut("a", 0, 123))
        assertFalse(registry.tryPut("a", 0, 456))
        assertTrue(registry.tryPut("b", 0, 456))
        assertTrue(registry.tryPut("a", 1, 789))
        assertFalse(registry.tryPut("a", 0, 456))
        assertEquals(123, registry.get("a", 0))
        assertEquals(456, registry.get("b", 0))
    }

    @Test
    fun overwrite() {
        val registry = VersionedRegistry<Int>()

        assertNull(registry.overwrite("a", 0, 123))
        assertEquals(123, registry.overwrite("a", 0, 456))

        assertNull(registry.overwrite("a", 1, 85))
        assertEquals(85, registry.overwrite("a", 1, 86))

        assertNull(registry.overwrite("b", 0, 321))
        assertEquals(321, registry.overwrite("b", 0, 123))

        assertEquals(456, registry.get("a", 0))
        assertEquals(86, registry.get("a", 1))
        assertEquals(86, registry.get("a", -1))
        assertEquals(123, registry.get("b", 0))
        assertEquals(123, registry.get("b", -1))

        assertNull(registry.get("a", 2))
        assertNull(registry.get("z", 0))
    }

    @Test
    fun precodnitions() {
        val registry = VersionedRegistry<Int>()
        registry.overwrite("a", 0, 123)

        assertFailsWith<IllegalArgumentException> {
            registry.overwrite("a", -2, 123)
        }
        assertFailsWith<IllegalArgumentException> {
            registry.tryPut("b", -3, 456)
        }
        assertFailsWith<IllegalArgumentException> {
            registry.remove("a", -2)
        }

        assertEquals(123, registry.get("a", 0))
        assertEquals(123, registry.get("a", -2))
        assertNull(registry.get("b", 0))
        assertNull(registry.get("b", -3))
    }

    @Test
    fun remove() {
        val registry = VersionedRegistry<Int>()

        registry.overwrite("a", 1, 111)
        registry.overwrite("a", 2, 222)
        registry.overwrite("b", 1, 333)

        assertEquals(222, registry.get("a", -1))

        assertNull(registry.remove("z", 0))
        assertEquals(222, registry.remove("a", 2))
        assertEquals(111, registry.get("a", -1))
        assertNull(registry.remove("a", 2))

        assertEquals(111, registry.remove("a", 1))
        assertNull(registry.nameToVersions["a"], "key 'a' should be removed from nameToVersions")
        assertNull(registry.remove("a", 1))

        assertEquals(333, registry.get("b", -1))
    }
}