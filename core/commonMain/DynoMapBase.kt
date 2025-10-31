package dyno

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Base interface for type-safe heterogeneous containers like [DynoMap] and [DynamicObject].
 *
 * Provides common operations for checking size, copying, and checking key presence by string name.
 */
interface DynoMapBase {
    /** The number of key-value pairs (properties) in this container. */
    val size: Int

    /** Returns a shallow copy of this container. */
    fun copy(): DynoMapBase

    /** Checks if a property with the given [key] name exists in this container. */
    operator fun contains(key: String): Boolean

    fun <T: Any> Unsafe.get(key: DynoKey<T>): T?

    /** Unlike [get], this method does not put deserialized value in [DynoMapImpl.data]. */
    fun <T: Any> Unsafe.getStateless(key: DynoKey<T>): T?

    fun Unsafe.contains(key: DynoKey<*>): Boolean

    /**
     * All methods marked with the [Unsafe] object ignore restrictions imposed by
     * subtypes of [DynoMapBase]. These operations should be used with caution,
     * as they can bypass compile-time type safety.
     */
    @InternalDynoApi
    object Unsafe
}

/**
 * Returns `true` if this container contains no key-value pairs; `false` otherwise.
 */
fun DynoMapBase.isEmpty(): Boolean = size == 0

/**
 * Returns `true` if this container contains at least one key-value pair; `false` otherwise.
 */
fun DynoMapBase.isNotEmpty(): Boolean = size != 0

/**
 * Returns `true` if this container is `null` or contains no key-value pairs; `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun DynoMapBase?.isNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmpty != null)
    }
    return (this ?: return true).isEmpty()
}