@file:Suppress("INVISIBLE_REFERENCE")

package dev.dokky.dyno

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.internal.Exact

/**
 * Mutable version of [DynamicObject].
 *
 * Usage:
 * ```
 * fun example(obj: MutableDynamicObject) {
 *     val key by dynoKey<String>()
 *     obj[key] = "value1"
 *     val previousValue: String? = obj.put(key, "value2")
 *     obj += key with "value3"
 * }
 * ```
 */
@Serializable(MutableDynamicObjectSerializer.Default::class)
sealed interface MutableDynamicObject: DynamicObject, MutableDynoMap<DynoKey<*>> {
    /**
     * Returns a new [MutableDynamicObject] containing all key-value pairs from the original map.
     */
    override fun copy(): MutableDynamicObject

    /**
     * Sets a [value] for the given [key].
     * If the [key] already exists, its value is replaced.
     * if [value] is null, the [key] is removed.
     */
    operator fun <@Exact T> set(key: DynoKey<T>, value: T?)

    /**
     * Puts a value for the given key and returns the previous value.
     * Returns `null` if the key didn't exist before.
     */
    fun <T> put(key: DynoKey<T>, value: @Exact T & Any): T?

    /**
     * Removes a value by key and returns it.
     */
    fun <T> remove(key: DynoKey<out T>): T?

    /**
     * Sets a value from an [entry].
     * If the `entry.value` is `null`, the key is removed.
     */
    fun set(entry: DynoEntry<DynoKey<*>, *>)

    /**
     * Puts a value from an [entry] and returns the previous value.
     */
    fun <T> put(entry: DynoEntry<DynoKey<T>, T>): T?

    /**
     * Puts all [entries] from the given collection.
     * Equivalent to calling [set] for each entry sequentially.
     */
    fun putAll(entries: Iterable<DynoEntry<DynoKey<*>, *>>)

    /**
     * Addition operator for entries.
     * Equivalent to calling [set] for the [entry].
     */
    operator fun plusAssign(entry: DynoEntry<DynoKey<*>, *>)

    /**
     * Subtraction operator for keys.
     * Equivalent to calling [remove] for the [key].
     */
    operator fun minusAssign(key: DynoKey<*>)
}

fun MutableDynamicObject(capacity: Int): MutableDynamicObject = DynamicObjectImpl(capacity)

@UnsafeDynoApi
fun MutableDynamicObject(data: MutableMap<Any, Any>?, json: Json?, readSafety: DynoReadSafety): MutableDynamicObject =
    DynamicObjectImpl(data, json, readSafety)

open class MutableDynamicObjectSerializer: DynoMapSerializerBase<MutableDynamicObject> {
    constructor(): super()
    constructor(readSafety: DynoReadSafety): super(readSafety = readSafety)

    companion object Default: MutableDynamicObjectSerializer()
}

/**
 * Returns a new [MutableDynamicObject] containing all key-value pairs from the original map.
 */
fun DynamicObject.toMutableDynamicObject(): MutableDynamicObject = DynamicObjectImpl(this)