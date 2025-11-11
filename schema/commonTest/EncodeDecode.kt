package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@Suppress("UNCHECKED_CAST")
internal fun Entity<*>.encodeDecode(
    serializer: KSerializer<*>,
    json: Json = Json
): MutableEntity<*> {
    serializer as KSerializer<DynoMapBase>

    val encodedString = json.encodeToString(serializer, this)
    val encodedJsonElement = json.encodeToJsonElement(serializer, this)
    val restoredJsonElement = Json.parseToJsonElement(encodedString)
    assertEquals(encodedJsonElement, restoredJsonElement)

    val decodedFromString = json.decodeFromString(serializer, encodedString)
        .also { (it as DynoMapImpl).validate() }

    return decodedFromString.unsafeCast()
}

private fun DynoMapImpl.validate() {
    for ((k,v) in DynoMapBase.Unsafe.data ?: return) {
        if (k is String) {
            assertNotNull(DynoMapBase.Unsafe.json, "key '$k' is serialized but Json instance is null")
            assertIs<JsonElement>(v, "key '$k' is serialized but value is not JsonElement")
        } else {
            assertIs<DynoKey<*>>(k, "unexpected type of key '$k'")
        }
    }
}