package dyno

import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * A base class for type-safe map-like structures where keys are derived from types or classes.
 *
 * Mapping is performed using the `serialName` of the class or type provided as the key.
 * The `serialName` is obtained via Kotlin serialization descriptors.
 *
 * This means that two classes with the same fully qualified name but different serial names
 * (e.g., due to `@SerialName` annotation) will be treated as distinct keys.
 *
 * Example usage:
 *
 * ```
 * val map = buildClassMap {
 *     put(A("value"))
 *     put(B(42))
 * }
 *
 * val a: A? = map.get<A>()
 * val b: B? = map[B::class]
 * ```
 *
 * Note: If a key's `serialName` conflicts with another, behavior is undefined
 * and may lead to unexpected overrides.
 */
sealed class ClassMapBase<in Base: Any>: DynoMapImpl, DynoMapBase {
    constructor(): super()
    constructor(capacity: Int): super(capacity)
    constructor(other: ClassMapBase<*>): super(other)
    constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    abstract override fun copy(): ClassMapBase<Base>

    /**
     * Retrieves a value associated with the serial name of the reified type [T].
     *
     * Returns `null` if no value is found for the type's serial name.
     */
    inline fun <reified T: Base> get(): T? = DynoMapBase.Unsafe.get(DynoTypeKey<T>())

    /**
     * Retrieves a value associated with the serial name of the specified [KClass] [key].
     *
     * Returns `null` if no value is found for the class's serial name.
     */
    operator fun <T : Base> get(key: KClass<out T>): T? = DynoMapBase.Unsafe.get(dynoKey(key))

    /**
     * Retrieves a value associated with the serial name of the specified [KType] [key].
     *
     * Returns `null` if no value is found for the type's serial name.
     */
    operator fun <T : Base> get(key: KType): T? = DynoMapBase.Unsafe.get(dynoKey(key))

    /**
     * Retrieves a value associated with the serial name of the reified type [T].
     *
     * Throws [NoSuchDynoKeyException] if no value is found.
     * @throws NoSuchDynoKeyException
     */
    inline fun <reified T: Base> getOrFail(): T {
        val key = DynoTypeKey<T>()
        return DynoMapBase.Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)
    }

    /**
     * Retrieves a value associated with the serial name of the specified [KClass] [key].
     *
     * Throws [NoSuchDynoKeyException] if no value is found.
     * @throws NoSuchDynoKeyException
     */
    fun <T : Base> getOrFail(key: KClass<out T>): T {
        val key = dynoKey(key)
        return DynoMapBase.Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)
    }

    /**
     * Retrieves a value associated with the serial name of the specified [KType] [key].
     *
     * Throws [NoSuchDynoKeyException] if no value is found.
     * @throws NoSuchDynoKeyException
     */
    fun <T : Base> getOrFail(key: KType): T {
        val key = dynoKey<T>(key)
        return DynoMapBase.Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)
    }

    /**
     * Retrieves a value associated with the serial name of the reified type [T],
     * or returns the result of [defaultValue] if not found.
     */
    inline fun <reified T: Base> getOrDefault(defaultValue: () -> T): T {
        val key = DynoTypeKey<T>()
        return DynoMapBase.Unsafe.get(key) ?: defaultValue()
    }

    /**
     * Retrieves a value associated with the serial name of the specified [KClass] [key],
     * or returns the result of [defaultValue] if not found.
     */
    fun <T : Base> getOrDefault(key: KClass<out T>, defaultValue: () -> T): T {
        val key = dynoKey(key)
        return DynoMapBase.Unsafe.get(key) ?: defaultValue()
    }

    /**
     * Checks whether a value exists for the serial name of the reified type [T].
     */
    inline fun <reified T : Base> contains(): Boolean = DynoMapBase.Unsafe.contains(DynoTypeKey<T>())

    /**
     * Checks whether a value exists for the serial name of the specified [KClass] [key].
     */
    operator fun <T : Base> contains(key: KClass<out T>): Boolean = DynoMapBase.Unsafe.contains(dynoKey(key))

    /**
     * Checks whether a value exists for the serial name of the specified [KType] [key].
     */
    operator fun <T : Base> contains(key: KType): Boolean = DynoMapBase.Unsafe.contains(dynoKey<Any>(key))

    /**
     * Returns a new [ClassMapBase] instance without the entry associated
     * with the serial name of the specified [KClass] [key].
     *
     * Does not modify the original map.
     */
    abstract operator fun <T: Base> minus(key: KClass<out T>): ClassMapBase<Base>

    /**
     * Returns a new [ClassMapBase] instance without the entry associated
     * with the serial name of the specified [KType] [key].
     *
     * Does not modify the original map.
     */
    abstract operator fun <T: Base> minus(key: KType): ClassMapBase<Base>
}