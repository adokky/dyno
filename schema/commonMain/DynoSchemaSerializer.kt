package dyno

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class DynoSchemaSerializer<S: TypedDynoSchema<S>>(val actualSchema: KSerializer<TypedDynoSchema<S>>): KSerializer<TypedDynoSchema<S>> {
    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor
    override fun serialize(encoder: Encoder, value: TypedDynoSchema<S>) =
        encoder.encodeString(value.schemaName)
    override fun deserialize(decoder: Decoder): TypedDynoSchema<S> =
        error("DynoSchema can not be decoded")
}