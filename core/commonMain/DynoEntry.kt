@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import kotlin.internal.Exact
import kotlin.jvm.JvmName

interface DynoEntry<out K: DynoKey<T>, T> {
    val key: K
    val value: T & Any
}

data class SimpleDynoEntry<out K: DynoKey<T>, T>(
    override val key: K,
    override val value: T & Any
): DynoEntry<K, T>


// simple constructors

fun <K: DynoKey<T>, @Exact T> DynoEntry(key: K, value: T & Any): DynoEntry<K, T> =
    SimpleDynoEntry(key, value)

infix fun <K: DynoKey<T>, @Exact T: Any> K.with(value: T): DynoEntry<K, T> =
    SimpleDynoEntry(this, value)

@JvmName("nullableWith")
infix fun <K: DynoKey<T?>, @Exact T: Any> K.with(value: T): DynoEntry<K, T?> =
    SimpleDynoEntry(this, value)