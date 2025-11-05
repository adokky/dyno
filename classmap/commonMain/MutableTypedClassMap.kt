package dyno

import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/**
 * Mutable version of [TypedClassMap] that allows adding, removing, and updating type-instance mappings
 * where the type is a subtype of [Base].
 */
@Serializable(MutableTypedClassMapSerializer::class)
open class MutableTypedClassMap<Base: Any>: TypedClassMap<Base>, MutableDynoMapBase {
    @PublishedApi internal constructor(): super()
    protected constructor(capacity: Int): super(capacity)
    internal constructor(other: TypedClassMap<Base>): super(other)
    internal constructor(other: ClassMap): super(other)
    internal constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    override fun copy(): MutableTypedClassMap<Base> = MutableTypedClassMap(this)

    /**
     * Associates the specified [value] with its class in the map.
     * @return The previous value associated with the key, or null if the key was not present.
     */
    @JvmName("putClassInstance")
    inline fun <reified T: Base> put(value: T): T? =
        Unsafe.put(dynoKey<T>(), value)

    /**
     * Associates the specified [value] with its class in the map.
     * @return The previous value associated with the key, or null if the key was not present.
     */
    @JvmName("setClassInstance")
    operator fun <T: Base> set(key: KClass<in T>, value: T) {
        // onAssign and onDecode default implementation does not care about type variance
        Unsafe.set(dynoKey(key.unsafeCast<KClass<T>>()), value)
    }

    /**
     * Removes the entry for the class of type [T] from the map.
     * @return The previous value associated with the key, or null if the key was not present.
     */
    @JvmName("removeClassInstance")
    inline fun <reified T: Base> remove(): T? =
        Unsafe.removeAndGet(dynoKey<T>())

    /**
     * Removes the entry for the specified [key] from the map.
     * @return The previous value associated with the key, or null if the key was not present.
     */
    @JvmName("removeClassInstance")
    fun <T: Base> remove(key: KClass<T>): T? =
        Unsafe.removeAndGet(dynoKey(key))

    /**
     * Returns the value for the class of type [T] if present, otherwise calls [defaultValue],
     * puts the result into the map, and returns it.
     * @param defaultValue The function to compute a default value.
     */
    @JvmName("getOrPutClassInstance")
    inline fun <reified T: Base> getOrPut(defaultValue: () -> T): T {
        val key = dynoKey<T>()
        Unsafe.getStateless(key)?.let { return it }
        return defaultValue().also { Unsafe.set(key, it) }
    }

    /**
     * Adds the specified [value] to the map using its class as the key.
     */
    @JvmName("addClassInstance")
    inline operator fun <reified T: Base> plusAssign(value: T) {
        Unsafe.put(dynoKey<T>(), value)
    }

    /**
     * Removes the entry for the specified [key] from the map.
     */
    @JvmName("deleteClassInstance")
    operator fun <T: Base> minusAssign(key: KClass<out T>) {
        Unsafe.remove(classMapStringKey(key))
    }
}