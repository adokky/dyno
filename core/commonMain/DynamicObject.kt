package dyno

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * Represents an automatically serializable object with a dynamically defined
 * set of strictly typed properties ([DynoKey]).
 * Provides a flexible way to work with structured data without predefined schema.
 *
 * This interface extends [DynoMap] with `DynoKey<*>` as key type and adds
 * convenient member operators, which do not require additional import.
 *
 * By default, [DynoMap] is deserialized "lazily" - each property is first deserialized
 * into a [kotlinx.serialization.json.JsonElement], and only when accessing specific properties
 * they are deserialized using [DynoKey.serializer].
 *
 * An alternative eager deserialization mode is available using [AbstractEagerDynoSerializer],
 * but it requires implementing [AbstractEagerDynoSerializer.resolve] function.
 *
 * Note that [DynoMap.hashCode] takes into account only the keys; the values are completely ignored.
 */
@Serializable(DynamicObjectSerializer::class)
sealed interface DynamicObject: DynoMap<DynoKey<*>> {
    /**
     * Creates a copy of this object.
     *
     * @return A new object with the same key-value pairs.
     */
    override fun copy(): DynamicObject

    /** Gets the value associated with the specified [key] or `null` if it is not present */
    @Suppress("INAPPLICABLE_JVM_NAME") // KT-31420
    @JvmName("getNullable")
    operator fun <T> get(key: DynoKey<T>): T?

    /**
     * Gets the value associated with the specified [key] or throws an exception if not found.
     * @throws NoSuchDynoKeyException if the [key] is not present.
     */
    operator fun <T: Any> get(key: DynoKey<T>): T

    /**
     * Gets the value associated with the specified [key] or throws an exception if not found.
     * @throws NoSuchDynoKeyException if the [key] is not present.
     */
    fun <T: Any> getOrFail(key: DynoKey<T?>): T

    /** Checks if the specified [key] is present in this object. */
    operator fun contains(key: DynoKey<*>): Boolean

    /** Creates a new object with the specified [entry] added or updated. */
    operator fun plus(entry: DynoEntry<DynoKey<*>, *>): DynamicObject

    /** Creates a new object with the specified [key] removed. */
    operator fun minus(key: DynoKey<*>): DynamicObject

    companion object {
        @JvmStatic
        val Empty: DynamicObject get() = EmptyDynamicObject
    }
}

internal object DynamicObjectSerializer: DynoMapSerializerBase<DynamicObject>()