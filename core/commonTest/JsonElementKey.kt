package dyno

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonElementKey: AbstractMutableDynoTest() {
    private val keyElement = DynoKey<JsonElement>("element")
    private val keyNumber = DynoKey<JsonElement>("number")

    @Test
    fun get() {
        val m = dynamicObjectOf(
            keyElement with JsonPrimitive("xyz"),
            keyNumber with JsonPrimitive(123)
        )

        fun DynamicObject.test() {
            assertEquals(JsonPrimitive("xyz"), this[keyElement])
            assertEquals(JsonPrimitive(123), this[keyNumber])
        }

        m.test()
        m.encodeDecode().test()
    }
}