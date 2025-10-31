package dyno

import dyno.DynoMapBase.Unsafe

interface MutableDynoMapBase: DynoMapBase {
    /** Removes all entries from the map, leaving it empty. */
    fun clear()

    override fun copy(): MutableDynoMapBase


    // Unsafe operations, hidden from simple direct usage.
    // All subtype restrictions are completely ignored

    fun <T: Any> Unsafe.set(key: DynoKey<T>, value: T)

    fun <T: Any> Unsafe.put(key: DynoKey<T>, value: T?): T?

    fun <T: Any> Unsafe.removeAndGet(key: DynoKey<T>): T?

    fun <T: Any> Unsafe.put(entry: DynoEntry<*, T?>): T?

    fun Unsafe.set(entry: DynoEntry<*, *>)

    fun Unsafe.remove(key: DynoKey<*>): Boolean

    fun Unsafe.remove(key: String): Boolean
}