package dyno

import kotlin.test.Test
import kotlin.test.assertEquals

class KeySetTest: AbstractDynoTest() {
    @Test
    fun readAllKeyNames() {
        val obj = dynamicObjectOf(e1, e2, e3) as DynamicObjectImpl
        assertEquals(setOf(p1.name, p2.name, p3.name), obj.keyNames.toSet())
        obj.remove(p2)
        assertEquals(setOf(p1.name, p3.name), obj.keyNames.toSet())
        obj[p2] = 343
        assertEquals(setOf(p1.name, p2.name, p3.name), obj.keyNames.toSet())
        obj.clear()
        assertEquals(emptyList(), obj.keyNames.toList())
    }
}