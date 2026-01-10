package dyno

import karamel.utils.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class CopyTest: AbstractDynoTest() {
    @Test
    fun plus_entry() {
        val obj = dynamicObjectOf(e1, e2, e3)
        assertNotNull(obj[p1])

        val me1 = p1.with(e1.value + "_modified")
        val copy = (obj + me1) as MutableDynamicObject
        assertEquals(dynamicObjectOf(e1, e2, e3), obj, "original map was modified via plus(entry)")
        assertEquals(me1.value, copy[p1])
        assertNotEquals(obj, copy)

        assertEquals(obj, copy + p1.with(e1.value))

        copy.clear()
        assertEquals(dynamicObjectOf(e1, e2, e3), obj, "original map was cleared via plus(entry)")

        assertEquals(dynamicObjectOf(e3, e1), dynamicObjectOf(e1, e2, e3) - e2.key)
    }

    private fun copyTest(funName: String, copy: (DynamicObject) -> MutableDynamicObject) {
        val original = dynamicObjectOf(e1, e2, e3).encodeDecode() as DynamicObject
        original[e2.key] // decode key

        val copy = copy(original)

        copy[e1.key] = "987"
        copy[e2.key] = 98765

        val errorMessage = "original map was modified via '$funName'"
        assertEquals(e1.value, original[e1.key], errorMessage)
        assertEquals(e2.value, original[e2.key], errorMessage)
        assertEquals(e3.value, original[e3.key], errorMessage)
    }

    @Test
    fun copying() {
        copyTest("copy") { it.copy().unsafeCast() }
    }

    @Test
    fun conversion_to_mutable() {
        copyTest("toMutableDynamicObject") { it.toMutableDynamicObject() }
    }
}