package dyno

import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Mutable version of [ClassMap] that allows adding, removing, and updating type-instance mappings.
 *
 * Mapping is performed using the `serialName` of the class or type provided as the key.
 * This means that two classes with the same fully qualified name but different serial names
 * (e.g., due to `@SerialName` annotation) will be treated as distinct keys.
 *
 * ```
 * val map = buildMutableClassMap {
 *     put("foo")
 *     put(42)
 * }
 *
 * map.get<String>() // returns "foo"
 * map.get<Int>() // returns 42
 * ```
 *
 * Warning: If a key's `serialName` conflicts with another, behavior is undefined
 * and may lead to unexpected overrides.
 *
 * @see ClassMap
 * @see TypedClassMap
 */
@Serializable(MutableClassMapSerializer::class)
class MutableClassMap: ClassMap, MutableDynoMapBase {
    @PublishedApi internal constructor(): super()
    internal constructor(capacity: Int): super(capacity)
    internal constructor(other: ClassMapBase<*>): super(other)
    internal constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    override fun copy(): MutableClassMap = MutableClassMap(this)

    /**
     * Associates the specified [value] with its type using the `serialName` of the type [T].
     * @return The previous value associated with the type, or `null` if the key was not present.
     */
    inline fun <reified T: Any> put(value: T): T? =
        Unsafe.put(DynoTypeKey<T>(), value)

    /**
     * Associates the specified [value] with the provided class [key] using the `serialName` of the class.
     * @return The previous value associated with the [key], or `null` if the [key] was not present.
     */
    fun <T: Any> put(key: KClass<in T>, value: T): T? {
        // onAssign and onDecode default implementation does not care about type variance
        return Unsafe.put(dynoKey(key.unsafeCast<KClass<T>>()), value)
    }

    /**
     * Associates the specified [value] with the provided class [key] using the `serialName` of the class.
     */
    operator fun <T: Any> set(key: KClass<in T>, value: T) {
        // onAssign and onDecode default implementation does not care about type variance
        Unsafe.set(dynoKey(key.unsafeCast<KClass<T>>()), value)
    }

    /**
     * Associates the specified [value] with the provided type [key] using the `serialName` of the type.
     */
    operator fun <T: Any> Unsafe.set(key: KType, value: T) {
        // onAssign and onDecode default implementation does not care about type variance
        Unsafe.set(dynoKey(key), value)
    }

    /**
     * Removes the entry for the type [T] from the map using the `serialName` of the type.
     * @return The previous value associated with the type, or `null` if the key was not present.
     */
    inline fun <reified T: Any> remove(): T? =
        Unsafe.removeAndGet(DynoTypeKey<T>())

    /**
     * Removes the entry for the specified class [key] from the map using the `serialName` of the class.
     * @return The previous value associated with the [key], or `null` if the key was not present.
     */
    fun <T: Any> remove(key: KClass<T>): T? =
        Unsafe.removeAndGet(dynoKey(key))

    /**
     * Removes the entry for the specified type [key] from the map using the `serialName` of the type.
     * @return The previous value associated with the [key], or `null` if the key was not present.
     */
    fun <T: Any> remove(key: KType): T? =
        Unsafe.removeAndGet(dynoKey(key))

    /**
     * Returns the value for the class of type [T] if present, otherwise calls [defaultValue],
     * puts the result into the map using the `serialName` of the type [T], and returns it.
     * @param defaultValue The function to compute a default value.
     */
    inline fun <reified T: Any> getOrPut(defaultValue: () -> T): T {
        val key = DynoTypeKey<T>()
        Unsafe.getStateless(key)?.let { return it }
        return defaultValue().also { Unsafe.set(key, it) }
    }

    /**
     * Adds the specified [value] to the map using the `serialName` of the type [T] as the key.
     */
    inline operator fun <reified T: Any> plusAssign(value: T) {
        Unsafe.put(DynoTypeKey<T>(), value)
    }

    /**
     * Removes the entry for the specified [key] from the map using the `serialName` of the class.
     */
    operator fun <T: Any> minusAssign(key: KClass<out T>) {
        Unsafe.remove(classMapStringKey(key))
    }

    /**
     * Removes the entry for the specified [key] from the map using the `serialName` of the type.
     */
    operator fun <T: Any> minusAssign(key: KType) {
        Unsafe.remove(classMapStringKey(key))
    }
}