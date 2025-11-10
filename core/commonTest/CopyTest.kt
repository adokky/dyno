package dyno

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CopyTest: AbstractDynoTest() {
    @Test
    fun copying() {
        val obj = dynamicObjectOf(e1, e2, e3)
        assertNotNull(obj[p1])

        val me1 = p1.with(e1.value + "_modified")
        val copy = (obj + me1) as MutableDynamicObject
        assertEquals(dynamicObjectOf(e1, e2, e3), obj, "original map was modified via 'copy'")
        assertEquals(me1.value, copy[p1])
        assertNotEquals(obj, copy)

        assertEquals(obj, copy + p1.with(e1.value))

        copy.clear()
        assertEquals(dynamicObjectOf(e1, e2, e3), obj, "original map was cleared via 'copy'")

        assertEquals(dynamicObjectOf(e3, e1), dynamicObjectOf(e1, e2, e3) - e2.key)
    }
}

