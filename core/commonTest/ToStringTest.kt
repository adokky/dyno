package dyno

import kotlin.test.Test
import kotlin.test.assertEquals

class ToStringTest: AbstractDynoTest() {
    @Test
    fun empty() {
        assertEquals("{}", dynamicObjectOf().toString())
        assertEquals("{}", buildDynamicObject { put(e1); remove(e1.key) }.toString())
    }

    @Test
    fun decoded() {
        val m = dynamicObjectOf(p1 with "X y Z", p2 with -893, p3 with listOf(true, false))
        assertEquals(
            """{p1="X y Z",p2=-893,p3=[true, false]}""",
            m.toString()
        )
    }

    @Test
    fun encoded() {
        val m = dynamicObjectOf(p1 with "X y Z", p2 with -893, p3 with listOf(true, false))
            .encodeDecode()
        assertEquals(
            """{p1="X y Z",p2=-893,p3=[true,false]}""",
            m.toString()
        )
    }
}