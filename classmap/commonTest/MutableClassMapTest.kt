package dyno

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MutableClassMapTest: AbstractClassMapTest() {
    @Test
    fun put_single_value() {
        val cm = buildMutableTypedClassMap {
            assertNull(put(a))
            assertNull(put(b))
            assertEquals(a, put(a))
            assertEquals(b, put(b))
        }

        assertEquals(a, cm.get<A>())
        assertEquals(b, cm.get<B>())
        assertNull(cm.get<Base>())
    }

    @Test
    fun untyped_put_single_value() {
        val cm = buildMutableClassMap {
            assertNull(put(a))
            assertNull(put(b))
            assertEquals(a, put(a))
            assertEquals(b, put(b))
        }

        assertEquals(a, cm.get<A>())
        assertEquals(b, cm.get<B>())
        assertNull(cm.get<Base>())
    }

    @Test
    fun untyped_remove() {
        val cm = buildMutableClassMap {}

        assertNull(cm.remove<A>())

        cm += a
        cm += b

        assertEquals(a, cm.remove<A>())
        assertEquals(b, cm.remove<B>())
        assertNull(cm.remove<Base>())
    }

    @Test
    fun remove() {
        val cm = buildMutableTypedClassMap<Base> {}

        assertNull(cm.remove<A>())

        cm += a
        cm += b

        assertEquals(a, cm.remove<A>())
        assertEquals(b, cm.remove<B>())
        assertNull(cm.remove<Base>())
    }

    @Test
    fun remove_klass() {
        val cm = buildMutableTypedClassMap<Base> {}

        assertNull(cm.remove(A::class))

        cm += a
        cm += b

        assertEquals(a, cm.remove(A::class))
        assertEquals(b, cm.remove(B::class))
        assertNull(cm.remove(Base::class))
    }

    @Test
    fun get_or_put() {
        val cm = buildMutableTypedClassMap<Base> {}

        assertEquals(a, cm.getOrPut { a })
        assertEquals(a, cm.getOrPut { A("xxx") })
        assertEquals(a, cm.get<A>())
        assertNull(cm.get<B>())

        cm.remove<A>()
        val newA = A("yyy")
        assertEquals(newA, cm.getOrPut { newA })
        assertEquals(newA, cm.getOrPut { A("zzz") })
        assertEquals(newA, cm.get<A>())
    }
}