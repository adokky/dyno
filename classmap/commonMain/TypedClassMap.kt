package dyno

import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * A container for mapping [kotlin.reflect.KClass] instances to values of type [Base].
 * The key must be a [kotlin.reflect.KClass] corresponding to serializable [Base] (sub)class.
 */
@Serializable(ClassMapSerializer::class)
sealed class TypedClassMap<in Base: Any>: DynoMapImpl, DynoMapBase {
    constructor(): super()
    constructor(capacity: Int): super(capacity)
    constructor(other: TypedClassMap<Base>): super(other)
    constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    abstract override fun copy(): TypedClassMap<Base>

    /**
     * Retrieves a value by [KClass] key for reified type [T].
     */
    @JvmName("getClassInstance")
    inline fun <reified T: Base> get(): T? =
        Unsafe.get(dynoKey<T>())

    /**
     * Retrieves a value by the specified [KClass] [key].
     */
    @JvmName("getClassInstance")
    operator fun <T : Base> get(key: KClass<out T>): T? {
        return Unsafe.get(dynoKey(key))
    }

    /**
     * Retrieves a value by [KClass] key for reified type [T] or throws [NoSuchDynoKeyException] if not found.
     * @throws NoSuchDynoKeyException
     */
    @JvmName("getClassInstanceOrFail")
    inline fun <reified T: Base> getOrFail(): T {
        val key = dynoKey<T>()
        return Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)
    }

    /**
     * Retrieves a value by the specified [KClass] [key] or throws [NoSuchDynoKeyException] if not found.
     * @throws NoSuchDynoKeyException
     */
    @JvmName("getClassInstanceOrFail")
    fun <T : Base> getOrFail(key: KClass<out T>): T {
        val key = dynoKey(key)
        return Unsafe.get(key) ?: throw NoSuchDynoKeyException(key)
    }

    /**
     * Retrieves a value by [KClass] key for reified type [T] or returns [defaultValue] if not found.
     */
    @JvmName("getClassInstanceOrDefault")
    inline fun <reified T: Base> getOrDefault(defaultValue: () -> T): T {
        val key = dynoKey<T>()
        return Unsafe.get(key) ?: defaultValue()
    }

    /**
     * Retrieves a value by the specified [KClass] [key] or returns [defaultValue] if not found.
     */
    @JvmName("getClassInstanceOrDefault")
    inline fun <T : Base> getOrDefault(key: KClass<out T>, defaultValue: () -> T): T {
        val key = dynoKey(key)
        return Unsafe.get(key) ?: defaultValue()
    }

    /**
     * Checks if the map contains a [KClass] key for reified type [T].
     */
    @JvmName("containsClassInstance")
    inline fun <reified T : Base> contains(): Boolean =
        Unsafe.contains(dynoKey<T>())

    /**
     * Checks if the map contains the specified [KClass] [key].
     */
    @JvmName("containsClassInstance")
    operator fun <T : Base> contains(key: KClass<out T>): Boolean =
        Unsafe.contains(dynoKey(key))

    /**
     * Returns a new [ClassMap] containing all elements of this map and the given [value].
     * The [value]'s class is used as the key.
     */
    @JvmName("plusClassInstance")
    inline operator fun <reified T: Base> plus(value: T): TypedClassMap<Base> =
        MutableTypedClassMap(this)
            .apply { Unsafe.put(dynoKey<T>(), value) }
            .unsafeCast()

    /**
     * Returns a new [ClassMap] containing all elements of this map except the entry with the [key].
     */
    @JvmName("minusClassInstance")
    operator fun <T: Base> minus(key: KClass<out T>): TypedClassMap<Base> =
        MutableTypedClassMap(this)
            .apply { Unsafe.remove(classMapStringKey(key)) }
            .unsafeCast()

    @PublishedApi
    internal fun <T: Base> classMapStringKey(klass: KClass<T>): String {
        @OptIn(ExperimentalSerializationApi::class)
        return kotlinx.serialization.serializer(klass, emptyList(), isNullable = false).descriptor.serialName
    }

    @PublishedApi
    internal inline fun <reified T: Base> dynoKey(): DynoKey<T> {
        val serializer = kotlinx.serialization.serializer<T>()
        return DynoKey(serializer.descriptor.serialName, serializer)
    }

    @PublishedApi
    internal fun <T: Base> dynoKey(klass: KClass<T>): DynoKey<T> {
        @OptIn(ExperimentalSerializationApi::class)
        val serializer = kotlinx.serialization.serializer(klass, emptyList(), isNullable = false)
        return DynoKey(serializer.descriptor.serialName, serializer.unsafeCast())
    }

    companion object {
        @JvmStatic
        fun <Base: Any> empty(): TypedClassMap<Base> = EMPTY.unsafeCast()
    }
}

private val EMPTY = MutableTypedClassMap<Any>(0)