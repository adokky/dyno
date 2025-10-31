package dyno

import kotlin.test.Test
import kotlin.test.assertEquals

class DynoBuilderTest: AbstractDynoTest() {
    private fun <R: DynamicObject> test(builder: (MutableDynamicObject.() -> Unit) -> R): R {
        val obj = builder {
            p1 set "xyz"
            p2 set 123
            p3 set listOf(false, true, false)
        }

        assertEquals("xyz", obj[p1])
        assertEquals(123, obj[p2])
        assertEquals(listOf(false, true, false), obj[p3])

        return obj
    }

    @Test
    fun readOnly() {
        test(::buildDynamicObject)
    }

    @Test
    fun mutable() {
        test(::buildMutableDynamicObject)
    }
}