@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import kotlin.internal.Exact
import kotlin.jvm.JvmName

interface DynoEntry<out K: DynoKey<V & Any>, out V> {
    val key: K
    val value: V
}

data class SimpleDynoEntry<out K: DynoKey<V & Any>, out V>(
    override val key: K,
    override val value: V
): DynoEntry<K, V>

// simple constructors

@Suppress("FunctionName")
fun <K: DynoKey<T>, T : Any> DynamicObjectEntry(key: K, value: @Exact T): DynoEntry<K, T> =
    SimpleDynoEntry(key, value)

@Suppress("FunctionName")
@JvmName("NullableDynamicObjectEntry")
fun <K: DynoKey<T>, T : Any> DynamicObjectEntry(key: K, value: @Exact T?): DynoEntry<K, T?> =
    SimpleDynoEntry(key, value)

// resolves ambiguity in case then `null` literal is passed
@Suppress("FunctionName")
@JvmName("NullDynamicObjectEntry")
fun <K: DynoKey<T>, T : Any> DynamicObjectEntry(key: K, value: Nothing?): DynoEntry<K, T?> =
    SimpleDynoEntry(key, value)

infix fun <K: DynoKey<T>, T : Any> K.with(value: @Exact T): DynoEntry<K, T> =
    SimpleDynoEntry(this, value)

@JvmName("withNullable")
infix fun <K: DynoKey<T>, T : Any> K.with(value: @Exact T?): DynoEntry<K, T?> =
    SimpleDynoEntry(this, value)

// resolves ambiguity in case then `null` literal is passed
@JvmName("withNull")
infix fun <K: DynoKey<T>, T : Any> K.with(value: Nothing?): DynoEntry<K, T?> =
    SimpleDynoEntry(this, value)