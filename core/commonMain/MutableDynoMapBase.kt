package dyno

import dyno.DynoMapBase.Unsafe

interface MutableDynoMapBase: DynoMapBase {
    /** Removes all entries from the map, leaving it empty. */
    fun clear()

    override fun copy(): MutableDynoMapBase


    // Unsafe operations, hidden from simple direct usage.
    // All subtype restrictions are completely ignored

    fun <T> Unsafe.set(key: DynoKey<T>, value: T & Any)

    fun <T> Unsafe.put(key: DynoKey<T>, value: T?): T?

    fun <T> Unsafe.removeAndGet(key: DynoKey<T>): T?

    fun <T> Unsafe.put(entry: DynoEntry<*, T>): T?

    fun Unsafe.set(entry: DynoEntry<*, *>)

    fun Unsafe.remove(key: DynoKey<*>): Boolean

    fun Unsafe.remove(key: String): Boolean
}