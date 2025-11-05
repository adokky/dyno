@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import dyno.DynoMapBase.Unsafe
import kotlin.internal.Exact
import kotlin.jvm.JvmName

/**
 * Represents a mutable map-like structure with typed keys conforming to [DynoKey].
 *
 * This interface extends both [MutableDynoMapBase] for general mutable map operations
 * and [DynoMap] for typed key-based access. It supports adding, removing, and updating
 * entries using typed keys and values with exact type matching.
 *
 * ## Usage
 *
 * The primary way to interact with a [MutableDynoMap] is through its extension functions.
 * These allow type-safe mutation of the map contents.
 *
 * Example:
 * ```
 * object Person {
 *     val name = DynoKey<String>("name")
 *     val age = DynoKey<Int>("age")
 * }
 *
 * val person: MutableDynoMap<DynoKey<*>> = mutableDynamicObjectOf()
 *
 * // Set values using get() operator
 * person[Person.name] = "Alex"
 * person[Person.age] = 30
 *
 * // Or using put() function
 * person.put(Person.name, "Bob")
 *
 * // Remove entries
 * val oldAge: Int? = person.remove(Person.age)
 *
 * // Use += and -= operators
 * person += Person.name with "Charlie"
 * person -= Person.age
 * ```
 */
interface MutableDynoMap<in K: DynoKey<*>>: MutableDynoMapBase, DynoMap<K> {
    /**
     * Creates a shallow copy of this map.
     *
     * Modifications to the copy do not affect the original map and vice versa.
     */
    override fun copy(): MutableDynoMap<K>
}

/**
 * Sets the [value] for the given [key] in the map.
 *
 * If [value] is `null`, the entry associated with [key] is removed from the map.
 */
operator fun <K: DynoKey<T>, T: Any> MutableDynoMap<K>.set(key: K, value: T?) {
    if (value == null) Unsafe.remove(key) else Unsafe.set(key, value)
}

/**
 * Sets the [value] for the serial name of [T] in the map.
 */
inline fun <reified T: Any> MutableDynoMap<DynoKey<*>>.setInstance(value: T) {
    Unsafe.set(DynoKey<T>(), value)
}

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * Returns the previous value associated with [key], or `null` if the [key] was not present.
 */
fun <K: DynoKey<T>, T: Any> MutableDynoMap<K>.put(key: K, value: @Exact T): T? =
    Unsafe.put(key, value)

/**
 * Removes the entry for the specified [key] from the map.
 *
 * Returns the previous value associated with [key], or `null` if the [key] was not present.
 */
fun <K: DynoKey<T>, T: Any> MutableDynoMap<K>.remove(key: K): T? =
    Unsafe.removeAndGet(key)

/**
 * Removes the entry for the serial name of specified type [T] from the map.
 *
 * Returns the previous value associated with [T] serial name,
 * or `null` if it was not present.
 */
inline fun <reified T: Any> MutableDynoMap<DynoKey<*>>.removeInstance(): T? =
    Unsafe.removeAndGet(DynoKey<T>())

/**
 * Sets the entry defined by [entry] in the map.
 *
 * This is equivalent to calling `set(entry.key, entry.value)`.
 */
fun <K: DynoKey<*>> MutableDynoMap<K>.set(entry: DynoEntry<K, *>) {
    Unsafe.set(entry)
}

/**
 * Associates the specified [entry] with its key in the map.
 *
 * Returns the previous value associated with the key from [entry], or `null` if the key was not present.
 */
fun <K: DynoKey<T>, T: Any> MutableDynoMap<K>.put(entry: DynoEntry<K, T?>): T? =
    Unsafe.put(entry)

/**
 * Associates the specified [value] with the serial name of [T] in the map.
 *
 * Returns the previous value associated with [T] serial name, or `null` if the key was not present.
 */
inline fun <reified T: Any> MutableDynoMap<DynoKey<*>>.putInstance(value: T): T? =
    put(DynoKey<T>(), value)

/**
 * Adds all [entries] to the map.
 *
 * Each entry is added using [put], so existing values are replaced and previous values are discarded.
 */
fun <K: DynoKey<*>> MutableDynoMap<K>.putAll(entries: Iterable<DynoEntry<K, *>>) {
    for (entry in entries) {
        Unsafe.set(entry)
    }
}

/**
 * Sets the [value] for the current context key in the map.
 *
 * This infix function allows DSL-style assignments when used inside [MutableDynoMap] context.
 * Example:
 * ```
 * with(mutableDynamicObjectOf()) {
 *     PersonKeys.name set "David"
 *     PersonKeys.age set 25
 * }
 * ```
 */
@JvmName("setValue")
context(obj: MutableDynoMap<K>)
infix fun <K: DynoKey<T>, T : Any> K.set(value: @Exact T) {
    obj[this] = value
}

/**
 * Adds the specified [entry] to the map.
 * Equivalent to calling [set] with the entry.
 */
operator fun <K: DynoKey<*>> MutableDynoMap<K>.plusAssign(entry: DynoEntry<K, *>) {
    Unsafe.set(entry)
}

/**
 * Removes the entry for the specified [key] from the map.
 * Equivalent to calling [remove] with the key.
 */
operator fun <K: DynoKey<*>> MutableDynoMap<K>.minusAssign(key: K) {
    Unsafe.remove(key)
}