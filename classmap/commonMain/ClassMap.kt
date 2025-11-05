package dyno

import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * A container for mapping [KClass] instances to values.
 * The key must be a [KClass] corresponding to any serializable class.
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
 *
 * Note: While [ClassMap] itself is not directly serializable via kotlinx.serialization,
 *  * it can be serialized automatically when used as a property in a `@Serializable` class.
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
     * The [value]'s class is used as the key.
     */
    @JvmName("plusClassInstance")
    inline operator fun <reified T: Any> plus(value: T): ClassMap =
        plus(dynoKey<T>(), value)

    @PublishedApi
    internal fun <T: Any> plus(key: DynoKey<T>, value: T): ClassMap =
        MutableClassMap(this)
            .apply { Unsafe.put(key, value) }
            .unsafeCast()

    /**
     * Returns a new [ClassMap] containing all elements of this map except the entry with the [key].
     */
    override fun <T : Any> minus(key: KClass<out T>): ClassMap {
        return MutableClassMap(this)
            .apply { Unsafe.remove(classMapStringKey(key)) }
            .unsafeCast()
    }

    /**
     * Returns a new [ClassMap] containing all elements of this map except the entry with the [key].
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

fun ClassMap.asTypedClassMap(): TypedClassMap<Any> = MutableTypedClassMap(Unsafe.data, Unsafe.json)

fun ClassMap.toTypedClassMap(): TypedClassMap<Any> = MutableTypedClassMap(this)

fun ClassMap.toMutableClassMap(): MutableClassMap = MutableClassMap(this)