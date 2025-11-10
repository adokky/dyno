package dyno

import dyno.DynoMapBase.Unsafe

interface MutableDynoMapBase: DynoMapBase {
    /** Removes all entries from the map, leaving it empty. */
    fun clear()

    override fun copy(): MutableDynoMapBase

    fun <T> Unsafe.set(key: DynoKey<T>, value: T & Any)

    fun <T> Unsafe.put(key: DynoKey<T>, value: T?): T?

    fun <T> Unsafe.removeAndGet(key: DynoKey<T>): T?

    fun <T> Unsafe.put(entry: DynoEntry<*, T>): T?

    fun Unsafe.set(entry: DynoEntry<*, *>)

    fun Unsafe.remove(key: DynoKey<*>): Boolean

    fun Unsafe.remove(key: String): Boolean
}