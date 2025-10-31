package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeCollection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer

/**
 * Abstract base class for serializing and deserializing [DynoMapBase] instances.
 *
 * Uses lazy deserialization strategy (values decoded upon first access).
 * This behavior is overridden in [AbstractEagerDynoSerializer].
 *
 * @see DynoMapSerializer
 */
abstract class AbstractDynoSerializer<T: DynoMapBase>: KSerializer<T> {
    /**
     * Serial descriptor for the [DynoMapBase].
     * Based on a map of [String] to [JsonElement].
     */
    final override val descriptor: SerialDescriptor = mapSerializer.descriptor

    /**
     * Deserializes a [DynoMapBase] from the given [decoder].
     *
     * @param decoder The decoder to read data from. Must be a [JsonDecoder].
     * @return A new [DynoMapBase] instance populated with deserialized data.
     * @throws IllegalStateException if [decoder] is not a [JsonDecoder].
     */
    final override fun deserialize(decoder: Decoder): T =
        deserialize(decoder as? JsonDecoder ?: incompatibleFormatError())

    open fun deserialize(decoder: JsonDecoder): T {
        return decoder.decodeStructure(descriptor) {
            val capacity = decodeCollectionSize(descriptor).let { if (it >= 0) it else 8 }
            val data = HashMap<Any, Any>(capacity)

            while (true) {
                val index = decodeElementIndex(descriptor)
                if (index == CompositeDecoder.DECODE_DONE) break

                val key = decodeStringElement(descriptor, index)
                val valueIndex = decodeElementIndex(descriptor)
                val value = decodeSerializableElement(descriptor, valueIndex, JsonElement.serializer())
                if (value != JsonNull) data[key] = value
            }

            createMap(data, decoder.json.takeUnless { data.isEmpty() })
        }
    }

    /**
     * Creates a [DynoMapBase] instance from the provided data and JSON context.
     *
     * @param data The deserialized data as a [HashMap].
     * @param json The [Json] instance used for decoding.
     */
    protected abstract fun createMap(data: MutableMap<Any, Any>?, json: Json?): T

    /**
     * Serializes the given [DynoMapBase] using the provided [encoder].
     *
     * @param encoder The encoder to write data to. Must be a [JsonEncoder].
     * @param value The [DynoMapBase] to serialize.
     * @throws IllegalStateException if [encoder] is not a [JsonEncoder].
     */
    final override fun serialize(encoder: Encoder, value: T) {
        serialize(
            encoder as? JsonEncoder ?: incompatibleFormatError(),
            value.unsafeCast<DynoMapImpl>()
        )
    }

    open fun serialize(encoder: JsonEncoder, value: DynoMapImpl) {
        val data = value.data ?: emptyHashMap

        encoder.encodeCollection(descriptor, data.size) {
            var index = 0
            for ((k, v) in data) {
                val keyName: String
                val serializer: KSerializer<*>

                if (k is DynoKey<*>) {
                    keyName = k.name
                    serializer = k.serializer
                } else {
                    keyName = k as String
                    serializer = JsonElement.serializer()
                }

                encodeStringElement(descriptor, index++, keyName)
                encodeSerializableElement(descriptor, index++, serializer.unsafeCast(), v)
            }
        }
    }

    private fun incompatibleFormatError(): Nothing = error("DynamicObject is compatible with JSON format only")

    private companion object {
        val emptyHashMap = HashMap<Any, Any>(0)
    }
}

internal sealed class DynoMapSerializerBase<T: DynoMapBase>: AbstractDynoSerializer<T>() {
    final override fun createMap(data: MutableMap<Any, Any>?, json: Json?): T =
        DynamicObjectImpl(data, json).unsafeCast()
}

private val mapSerializer = serializer<HashMap<String, JsonElement>>()