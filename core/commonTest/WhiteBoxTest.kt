package dyno

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WhiteBoxTest: AbstractMutableDynoTest() {
    @Test
    fun test() {
        val original = mutableDynamicObjectOf(e1, e2, e3) as DynamicObjectImpl
        assertNull(with(original) { DynoMapBase.Unsafe.json })

        val decoded = original.encodeDecode()

        assertNotNull(with(decoded) { DynoMapBase.Unsafe.json })
        val decodedData = with(decoded) { DynoMapBase.Unsafe.data }
        assertNotNull(decodedData)

        assertEquals(JsonPrimitive("test"), decodedData[e1.key.name])
        assertEquals(JsonPrimitive(77), decodedData[e2.key.name])
        assertEquals(JsonArray(listOf(JsonPrimitive(true))), decodedData[e3.key.name])

        decoded[e1.key]
        assertNull(decodedData[e1.key.name])
        assertEquals(e1.value, decodedData[e1.key])
        assertNotNull(with(decoded) { DynoMapBase.Unsafe.json })

        decoded.remove(e2.key)
        assertNull(decodedData[e2.key.name])
        assertNull(decodedData[e2.key])

        val newV = listOf(false, false, false)
        decoded.put(e3.key, newV)
        assertNull(decodedData[e3.key.name])
        assertEquals(newV, decodedData[e3.key])

        assertEquals(2, decodedData.size)
    }
}