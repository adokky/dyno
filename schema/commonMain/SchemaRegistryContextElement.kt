package dyno

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.jvm.JvmStatic

internal class ContextElement(val registry: DynoSchemaRegistry): KSerializer<ContextElement> {
    override val descriptor get() = serialDescriptor
    override fun deserialize(decoder: Decoder) = serializationNotSupported()
    override fun serialize(encoder: Encoder, value: ContextElement) = serializationNotSupported()
    private fun serializationNotSupported(): Nothing = error("ContextElement is not serializable")

    private companion object {
        @JvmStatic
        val serialDescriptor = buildClassSerialDescriptor("/~KeyProviderContextElement~/")
    }
}

/**
 * Registers a [SimpleSchemaRegistry] into the current [kotlinx.serialization.modules.SerializersModuleBuilder].
 *
 * This is typically called internally by the DSL function [dynoSchemaRegistry].
 */
fun SerializersModuleBuilder.dynoSchemaRegistry(registry: DynoSchemaRegistry) {
    contextual(ContextElement::class, ContextElement(registry))
}