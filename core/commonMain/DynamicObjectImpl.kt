@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlinx.serialization.json.Json

internal open class DynamicObjectImpl: DynoMapImpl, MutableDynamicObject {
    constructor(): super()
    constructor(capacity: Int): super(capacity)
    constructor(entries: Collection<DynoEntry<*, *>>): super(entries)
    constructor(other: DynoMapBase): super(other)
    constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    override fun copy(): DynamicObjectImpl = DynamicObjectImpl(this)

    override fun <T: Any> get(key: DynoKey<T>): T? =
        Unsafe.get(key)

    override fun <T: Any> getOrFail(key: DynoKey<T>): T =
        Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)

    override fun contains(key: DynoKey<*>): Boolean =
        Unsafe.contains(key)

    override fun plus(entry: DynoEntry<DynoKey<*>, *>): DynamicObject =
        DynamicObjectImpl(this).also { it += entry }

    override fun minus(key: DynoKey<*>): DynamicObject =
        DynamicObjectImpl(this).also { it -= key }

    override fun <T: Any> set(key: DynoKey<T>, value: T?) {
        if (value == null) Unsafe.remove(key) else Unsafe.set(key.unsafeCast(), value)
    }

    override fun <T: Any> put(key: DynoKey<T>, value: T): T? =
        Unsafe.put(key, value)

    override fun <T: Any> remove(key: DynoKey<out T>): T? =
        Unsafe.removeAndGet(key)

    override fun set(entry: DynoEntry<DynoKey<*>, *>) {
        Unsafe.set(entry)
    }

    override fun <T: Any> put(entry: DynoEntry<DynoKey<T>, T?>): T? =
        Unsafe.put(entry)

    override fun putAll(entries: Iterable<DynoEntry<DynoKey<*>, *>>) {
        for (entry in entries) {
            Unsafe.set(entry)
        }
    }

    override operator fun plusAssign(entry: DynoEntry<DynoKey<*>, *>) {
        Unsafe.set(entry)
    }

    override operator fun minusAssign(key: DynoKey<*>) {
        Unsafe.remove(key)
    }
}