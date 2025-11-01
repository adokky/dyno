package dyno

import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.KType

sealed class ClassMapBase<in Base: Any>: DynoMapImpl, DynoMapBase {
    constructor(): super()
    constructor(capacity: Int): super(capacity)
    constructor(other: ClassMapBase<*>): super(other)
    constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    abstract override fun copy(): ClassMapBase<Base>

    /**
     * Retrieves a value by [KClass] key for reified type [T].
     */
    inline fun <reified T: Base> get(): T? = DynoMapBase.Unsafe.get(dynoKey<T>())

    /**
     * Retrieves a value by the specified [KClass] [key].
     */
    operator fun <T : Base> get(key: KClass<out T>): T? = DynoMapBase.Unsafe.get(dynoKey(key))

    /**
     * Retrieves a value by the specified [KType] [key].
     */
    operator fun <T : Base> get(key: KType): T? = DynoMapBase.Unsafe.get(dynoKey(key))

    /**
     * Retrieves a value by type [T] or throws [NoSuchDynoKeyException] if not found.
     * @throws NoSuchDynoKeyException
     */
    inline fun <reified T: Base> getOrFail(): T {
        val key = dynoKey<T>()
        return DynoMapBase.Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)
    }

    /**
     * Retrieves a value by the specified [KClass] [key] or throws [NoSuchDynoKeyException] if not found.
     * @throws NoSuchDynoKeyException
     */
    fun <T : Base> getOrFail(key: KClass<out T>): T {
        val key = dynoKey(key)
        return DynoMapBase.Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)
    }

    /**
     * Retrieves a value by the specified [KType] [key] or throws [NoSuchDynoKeyException] if not found.
     * @throws NoSuchDynoKeyException
     */
    fun <T : Base> getOrFail(key: KType): T {
        val key = dynoKey<T>(key)
        return DynoMapBase.Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)
    }

    /**
     * Retrieves a value for type [T] or returns [defaultValue] if not found.
     */
    inline fun <reified T: Base> getOrDefault(defaultValue: () -> T): T {
        val key = dynoKey<T>()
        return DynoMapBase.Unsafe.get(key) ?: defaultValue()
    }

    /**
     * Retrieves a value by the specified [KClass] [key] or returns [defaultValue] if not found.
     */
    fun <T : Base> getOrDefault(key: KClass<out T>, defaultValue: () -> T): T {
        val key = dynoKey(key)
        return DynoMapBase.Unsafe.get(key) ?: defaultValue()
    }

    /**
     * Checks if the map contains a [KClass] key for reified type [T].
     */
    inline fun <reified T : Base> contains(): Boolean = DynoMapBase.Unsafe.contains(dynoKey<T>())

    /**
     * Checks if the map contains the specified [KClass] [key].
     */
    operator fun <T : Base> contains(key: KClass<out T>): Boolean = DynoMapBase.Unsafe.contains(dynoKey(key))

    /**
     * Checks if the map contains the specified [KType] [key].
     */
    operator fun <T : Base> contains(key: KType): Boolean = DynoMapBase.Unsafe.contains(dynoKey<Any>(key))

    /**
     * Returns a new [ClassMapBase] containing all elements of this map except the entry with the [key].
     */
    abstract operator fun <T: Base> minus(key: KClass<out T>): ClassMapBase<Base>

    /**
     * Returns a new [ClassMapBase] containing all elements of this map except the entry with the [key].
     */
    abstract operator fun <T: Base> minus(key: KType): ClassMapBase<Base>
}