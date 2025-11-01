package dyno

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ConversionTest: AbstractClassMapTest() {
    @Test
    fun to_class_map() {
        val original = buildMutableTypedClassMap {
            put(A("1"))
            put(B(2))
        }

        val copy = original.toClassMap()

        fun check() {
            assertEquals(A("1"), copy.getOrFail())
            assertEquals(B(2), copy.getOrFail())
        }

        check()
        assertEquals<DynoMapBase>(copy, original)
        original += B(3)
        check() // check copy was not modified
        assertNotEquals<DynoMapBase>(copy, original)
    }

    @Test
    fun to_mutable_typed_class_map() {
        val original = buildMutableTypedClassMap {
            put(A("1"))
            put(B(2))
        }

        val copy = original.toMutableTypedClassMap()

        fun check() {
            assertEquals(A("1"), copy.getOrFail())
            assertEquals(B(2), copy.getOrFail())
        }

        check()
        assertEquals<DynoMapBase>(copy, original)

        original += B(3)
        check()
        assertNotEquals<DynoMapBase>(copy, original)

        copy += B(4)
        copy += A("7")
        // check original was not modified
        assertEquals(A("1"), original.getOrFail())
        assertEquals(B(3), original.getOrFail())
    }

    @Test
    fun as_class_map() {
        val tcm = buildMutableTypedClassMap {
            put(A("1"))
            put(B(2))
        }

        val cm = tcm.asClassMap()

        assertEquals(A("1"), cm.getOrFail())
        assertEquals(B(2), cm.getOrFail())
        assertEquals<DynoMapBase>(cm, tcm)

        tcm += B(3)
        assertEquals(A("1"), cm.getOrFail())
        assertEquals(B(3), cm.getOrFail())
        assertEquals<DynoMapBase>(cm, tcm)
    }
}