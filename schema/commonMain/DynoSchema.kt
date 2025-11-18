package dyno

import kotlinx.serialization.modules.SerializersModule

/**
 * Interface for providing [DynoKey] instances by their string names.
 *
 * Used by [PolymorphicDynoSerializer] to resolve keys during deserialization.
 */
@DynoDslMarker
interface DynoSchema {
    // DynoSchema should not have any properties or property-like declarations,
    // as those could conflict with user-defined ones.

    fun name(): String

    fun version(): Int

    /**
     * Retrieves a [DynoKey] instance corresponding to the given key name within the specified [SerializersModule],
     * or `null` if not found.
     * @param serializersModule The [SerializersModule] that may contain registered serializers and key mappings.
     * @param name The key name.
     */
    fun getKey(serializersModule: SerializersModule, name: String): DynoKey<*>?

    fun keys(): Collection<DynoKey<*>>

    /** Number of keys registered in this schema */
    fun keyCount(): Int = keys().size
}

