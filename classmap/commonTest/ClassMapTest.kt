package dyno

import kotlin.test.*

class ClassMapTest: AbstractClassMapTest() {
    private val ucm = buildClassMap {
        put(a)
        put(b)
    }

    private val cm = ucm.asTypedClassMap()

    @Test
    fun dynoName() {
        println(DynoKey<List<Int>>())
    }

    @Test
    fun simple_get() {
        assertEquals(a, cm.get<A>())
        assertEquals(b, cm.get<B>())

        assertEquals(a, cm[A::class])
        assertEquals(b, cm[B::class])

        assertNull(cm.get<Base>())
    }

    @Test
    fun get_or_fail() {
        assertEquals(a, cm.getOrFail<A>())
        assertEquals(b, cm.getOrFail<B>())

        assertEquals(a, cm.getOrFail(A::class))
        assertEquals(b, cm.getOrFail(B::class))

        assertFailsWith<NoSuchDynoKeyException> {
            cm.getOrFail<Base>()
        }.also { ex ->
            assertEquals("Base", ex.key)
        }
    }

    @Test
    fun get_or_default() {
        assertEquals(a, cm.getOrDefault { A("error") })
        assertEquals(b, cm.getOrDefault { B(456) })

        assertEquals(a, cm.getOrDefault(A::class) { A("error") })
        assertEquals(b, cm.getOrDefault(B::class) { B(456) })

        assertEquals(A("default"), cm.getOrDefault<Base> { A("default") })
        assertEquals(A("default"), cm.getOrDefault(Base::class) { A("default") })
    }

    @Test
    fun untyped_get_or_default() {
        assertEquals(a, ucm.getOrDefault { A("error") })
        assertEquals(b, ucm.getOrDefault { B(456) })

        assertEquals(a, ucm.getOrDefault(A::class) { A("error") })
        assertEquals(b, ucm.getOrDefault(B::class) { B(456) })

        assertEquals(A("default"), ucm.getOrDefault<Base> { A("default") })
        assertEquals(A("default"), ucm.getOrDefault(Base::class) { A("default") })
    }

    @Test
    fun untyped_simple_get() {
        assertEquals(a, ucm.get<A>())
        assertEquals(b, ucm.get<B>())

        assertEquals(a, ucm[A::class])
        assertEquals(b, ucm[B::class])

        assertNull(ucm.get<Base>())
    }

    @Test
    fun untyped_get_or_fail() {
        assertEquals(a, ucm.getOrFail<A>())
        assertEquals(b, ucm.getOrFail<B>())

        assertEquals(a, ucm.getOrFail(A::class))
        assertEquals(b, ucm.getOrFail(B::class))

        assertFailsWith<NoSuchDynoKeyException> {
            ucm.getOrFail<Base>()
        }.also { ex ->
            assertEquals("Base", ex.key)
        }
    }

    @Test
    fun key_existence() {
        assertTrue(ucm.contains<A>())
        assertFalse(ucm.contains<Base>())

        assertTrue(ucm.contains(A::class))
        assertFalse(ucm.contains(Base::class))

        assertTrue(cm.contains<A>())
        assertFalse(cm.contains<Base>())

        assertTrue(cm.contains(A::class))
        assertFalse(cm.contains(Base::class))
    }

    @Test
    fun plus() {
        val cm2 = cm + B(2)
        assertEquals(a, cm2.get<A>())
        assertEquals(B(2), cm2.get<B>())

        val ucm2 = ucm + A("2")
        assertEquals(A("2"), ucm2.get<A>())
        assertEquals(b, ucm2.get<B>())

        // check the original is unchanged
        assertEquals(a, cm.get<A>())
        assertEquals(b, cm.get<B>())
    }

    @Test
    fun minus_key() {
        (cm - B::class).apply {
            assertEquals(a, get<A>())
            assertNull(get<B>())
        }

        (ucm - A::class).apply {
            assertNull(get<A>())
            assertEquals(b, get<B>())
        }

        (cm - Base::class).apply {
            assertEquals(a, get<A>())
            assertEquals(b, get<B>())
        }

        (ucm - Base::class).apply {
            assertEquals(a, get<A>())
            assertEquals(b, get<B>())
        }

        // check the original is unchanged
        assertEquals(a, cm.get<A>())
        assertEquals(b, cm.get<B>())
    }
}

