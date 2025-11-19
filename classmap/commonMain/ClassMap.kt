package dyno

import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * A container for mapping [KClass] instances to values.
 * The key must be a [KClass] corresponding to any serializable class.
 *
 * Mapping is performed using the `serialName` of the class or type provided as the key.
 * This means that two classes with the same fully qualified name but different serial names
 * (e.g., due to `@SerialName` annotation) will be treated as distinct keys.
 *
 * ```
 * val map: ClassMap = buildClassMap {
 *     put("foo")
 *     put(42)
 * }
 *
 * map.get<String>() // returns "foo"
 * map.get<Int>() // returns 42
 * ```
 * Warning: If a key's `serialName` conflicts with another, behavior is undefined
 * and may lead to unexpected overrides.
 *
 * @see TypedClassMapSerializer
 */
@Serializable(ClassMapSerializer::class)
sealed class ClassMap: ClassMapBase<Any> {
    constructor(): super()
    constructor(capacity: Int): super(capacity)
    constructor(other: ClassMapBase<*>): super(other)
    constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    abstract override fun copy(): ClassMap

    /**
     * Returns a new [ClassMap] containing all elements of this map and the given [value].
     * The key is determined by the `serialName` of the [value]'s class.
     * If an entry with the same `serialName` already exists, it will be replaced.
     */
    inline operator fun <reified T: Any> plus(value: T): ClassMap =
        plus(DynoTypeKey<T>(), value)

    @PublishedApi
    internal fun <T: Any> plus(key: DynoKey<T>, value: T): ClassMap =
        MutableClassMap(this)
            .apply { Unsafe.put(key, value) }
            .unsafeCast()

    /**
     * Returns a new [ClassMap] containing all elements of this map
     * except the entry with the given [key] `serialName`.
     * If no entry with the matching `serialName` exists,
     * the returned map will be equal to the original.
     */
    override fun <T : Any> minus(key: KClass<out T>): ClassMap {
        return MutableClassMap(this)
            .apply { Unsafe.remove(classMapStringKey(key)) }
            .unsafeCast()
    }

    /**
     * Returns a new [ClassMap] containing all elements of this map
     * except the entry with the given [key] `serialName`.
     * If no entry with the matching `serialName` exists,
     * the returned map will be equal to the original.
     */
    override fun <T : Any> minus(key: KType): ClassMap {
        return MutableClassMap(this)
            .apply { Unsafe.remove(classMapStringKey(key)) }
            .unsafeCast()
    }

    object Empty: ClassMap(0) {
        override fun copy(): Empty = this
    }
}

/**
 * Converts this [ClassMap] to a [TypedClassMap] with the same contents.
 *
 * This is a view conversion - both maps will share the same underlying data.
 */
fun ClassMap.asTypedClassMap(): TypedClassMap<Any> = MutableTypedClassMap(Unsafe.data, Unsafe.json)

/**
 * Creates a new [TypedClassMap] with the same contents as this [ClassMap].
 *
 * This is a copy conversion - modifications to the returned map will not affect the original.
 */
fun ClassMap.toTypedClassMap(): TypedClassMap<Any> = MutableTypedClassMap(this)

/**
 * Creates a new [MutableClassMap] with the same contents as this [ClassMap].
 *
 * This is a copy conversion - modifications to the returned map will not affect the original.
 */
fun ClassMap.toMutableClassMap(): MutableClassMap = MutableClassMap(this)