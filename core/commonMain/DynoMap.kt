package dyno

import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName

/**
 * Type-safe, serializable, heterogeneous map where each key is associated with a specific value type.
 *
 * Public methods for accessing data in [DynoMap] and [MutableDynoMap] are implemented as extension
 * functions which allows to restrict input key types to upper bound [K].
 *
 * By default, [DynoMap] is deserialized "lazily" - each property is first deserialized
 * into a [kotlinx.serialization.json.JsonElement], and only when accessing specific properties
 * they are deserialized using [DynoKey.serializer].
 *
 * An alternative eager deserialization mode is available using [AbstractEagerDynoSerializer],
 * but it requires implementing [AbstractEagerDynoSerializer.resolve] function.
 *
 * Note that [DynoMap.hashCode] takes into account only the keys; the values are completely ignored.
 *
 * @see DynoClassKey
 * @see AbstractEagerDynoSerializer
 */
@Serializable(DynoMapSerializer::class)
interface DynoMap<in K: DynoKey<*>>: DynoMapBase


/** Gets the value associated with the specified [key] or `null` if not found */
@JvmName("getNullable")
operator fun <K: DynoKey<T>, T> DynoMap<K>.get(key: K): T? =
    Unsafe.get(key)

/**
 * Gets the value associated with the specified [key] or throws [NoSuchDynoKeyException] if not found.
 * @throws NoSuchDynoKeyException if the [key] is not present.
 */
operator fun <K: DynoKey<T>, T: Any> DynoMap<K>.get(key: K): T =
    Unsafe.getOrFail(key)

/**
 * Gets the value associated with the serial name of [T] or `null` if not found.
 */
inline fun <reified T: Any> DynoMap<DynoKey<*>>.getInstance(): T? =
    Unsafe.get(DynoClassKey<T>())

/**
 * Retrieves a value of type [T] by its serial name or throws [NoSuchDynoKeyException] if not found.
 * @throws NoSuchDynoKeyException if key with serial name of [T] is not found.
 */
inline fun <reified T: Any> DynoMap<DynoKey<*>>.getInstanceOrFail(): T =
    getOrFail(DynoClassKey<T?>())

/**
 * Gets the value associated with the specified [key] or throws [NoSuchDynoKeyException] if not found.
 * @throws NoSuchDynoKeyException if the [key] is not present.
 */
fun <K: DynoKey<T?>, T: Any> DynoMap<K>.getOrFail(key: K): T =
    Unsafe.getOrFail(key)

/**
 * Checks if the specified [key] is present in this map.
 */
operator fun <K: DynoKey<*>> DynoMap<K>.contains(key: K): Boolean =
    Unsafe.contains(key)

/**
 *  Creates a new object with the specified [entry] added or updated.
 */
operator fun <K: DynoKey<*>> DynoMap<K>.plus(entry: DynoEntry<K, *>): DynoMap<K> =
    DynamicObjectImpl(this).also { it += entry }.unsafeCast()

/**
 * Creates a new object with the specified [key] removed.
 */
operator fun <K: DynoKey<*>> DynoMap<K>.minus(key: K): DynoMap<K> =
    DynamicObjectImpl(this).also { it -= key }.unsafeCast()


internal object DynoMapSerializer: DynoMapSerializerBase<DynoMap<DynoKey<*>>>()