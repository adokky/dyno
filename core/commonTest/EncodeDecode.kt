package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@Suppress("UNCHECKED_CAST")
internal fun DynoMapBase.encodeDecode(serializer: KSerializer<*> = DynoMapSerializer, json: Json = Json): DynamicObjectImpl {
    serializer as KSerializer<DynoMapBase>

    val encodedString = json.encodeToString(serializer, this)
    val encodedJsonElement = json.encodeToJsonElement(serializer, this)
    val restoredJsonElement = Json.parseToJsonElement(encodedString)
    assertEquals(encodedJsonElement, restoredJsonElement)

    val decodedFromString = json.decodeFromString<DynamicObjectImpl>(serializer.unsafeCast(), encodedString)
        .also { it.validate() }
    val decodedFromJsonElement = json.decodeFromJsonElement<DynamicObjectImpl>(serializer.unsafeCast(), restoredJsonElement)
        .also { it.validate() }
    assertEquals(decodedFromString, decodedFromJsonElement)

    return decodedFromString
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

abstract class TestEagerSerializer<T : DynoMapBase>: AbstractEagerDynoSerializer<T>() {
    protected val keys = HashMap<String, DynoKey<*>>()

    protected fun register(key: DynoKey<*>) {
        keys[key.name] = key
    }

    protected inline fun <reified T : Any> key(name: String): DynoKey<T> {
        return DynoKey<T>(name).also { register(it) }
    }

    protected open fun defaultResolveResult(key: String): ResolveResult = ResolveResult.Keep

    override fun resolve(context: ResolveContext): ResolveResult {
        return keys[context.keyString] ?: defaultResolveResult(context.keyString)
    }

    override fun createMap(state: Any?, data: MutableMap<Any, Any>?, json: Json?): T =
        DynamicObjectImpl(data, json).unsafeCast()
}