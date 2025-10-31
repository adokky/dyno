package dyno

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.jvm.JvmStatic

/*
    TODO module DSL: newSchema("name") { ... } and extendSchema("name") { ... }
 */

// todo rename to Schema?
/**
 * Interface for providing [DynoKey] instances by their string names.
 *
 * Used by [AbstractPolymorphicDynoSerializer] to resolve keys during deserialization.
 */
interface DynoKeyProvider {
    /**
     * Retrieves a [DynoKey] instance corresponding to the given key name within the specified [SerializersModule],
     * or `null` if not found.
     * @param serializersModule The [SerializersModule] that may contain registered serializers and key mappings.
     */
    fun getDynoKey(serializersModule: SerializersModule, name: String): DynoKey<*>?

    val name: String
}

/**
 * Registry holding a mapping from schema IDs to [DynoKeyProvider]s,
 * and an optional default provider.
 *
 * Used by [AbstractPolymorphicDynoSerializer] to resolve keys during deserialization.
 */
class DynamicObjectKeyRegistry(
    /**
     * Function that returns a [DynoKeyProvider] for a given schema ID, or null if not found.
     */
    val schemaToProvider: (String) -> DynoKeyProvider?,
    /**
     * Default [DynoKeyProvider] used when no schema-specific provider is found.
     */
    val default: DynoKeyProvider? = null
) {
    /**
     * Internal context element used to pass the registry through the serialization context.
     */
    internal class ContextElement(val registry: DynamicObjectKeyRegistry): KSerializer<ContextElement> {
        override val descriptor get() = serialDescriptor
        override fun deserialize(decoder: Decoder) = serializationNotSupported()
        override fun serialize(encoder: Encoder, value: ContextElement) = serializationNotSupported()
        private fun serializationNotSupported(): Nothing = error("ContextElement is not serializable")

        private companion object {
            @JvmStatic
            val serialDescriptor = buildClassSerialDescriptor("/~KeyProviderContextElement~/")
        }
    }
}

/**
 * Registers a [DynamicObjectKeyRegistry] into the current [SerializersModuleBuilder].
 *
 * This is typically called internally by the DSL function [dynamicObjectKeys].
 */
fun SerializersModuleBuilder.dynamicObjectKeys(registry: DynamicObjectKeyRegistry) {
    contextual(
        DynamicObjectKeyRegistry.ContextElement::class,
        DynamicObjectKeyRegistry.ContextElement(registry)
    )
}