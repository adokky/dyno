package dyno

import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * A container for mapping serializable type [Base] or `KClass<Base>` to instances of [Base] (sub)class.
 */
@Serializable(TypedClassMapSerializer::class)
sealed class TypedClassMap<in Base: Any>: ClassMapBase<Any>, DynoMapBase {
    constructor(): super()
    constructor(capacity: Int): super(capacity)
    constructor(other: TypedClassMap<Base>): super(other)
    constructor(other: ClassMap): super(other)
    constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    abstract override fun copy(): TypedClassMap<Base>

    /**
     * Returns a new [TypedClassMap] containing all elements of this map and the given [value].
     * The key is determined by the `serialName` of the [value]'s class.
     * If an entry with the same `serialName` already exists, it will be replaced.
     */
    @JvmName("plusClassInstance")
    inline operator fun <reified T: Base> plus(value: T): TypedClassMap<Base> =
        plus(DynoTypeKey<T>(), value)

    @PublishedApi
    internal fun <T: Base> plus(key: DynoKey<T>, value: T): TypedClassMap<Base> =
        MutableTypedClassMap(this)
            .apply { Unsafe.put(key, value) }
            .unsafeCast()

    /**
     * Returns a new [TypedClassMap] containing all elements of this map
     * except the entry with the given [key] `serialName`.
     * If no entry with the matching `serialName` exists,
     * the returned map will be equal to the original.
     */
    override fun <T : Any> minus(key: KClass<out T>): TypedClassMap<Any> =
        MutableTypedClassMap(this)
            .apply { Unsafe.remove(classMapStringKey(key)) }
            .unsafeCast()

    /**
     * Returns a new [TypedClassMap] containing all elements of this map
     * except the entry with the given [key] `serialName`.
     * If no entry with the matching `serialName` exists,
     * the returned map will be equal to the original.
     */
    override fun <T : Any> minus(key: KType): TypedClassMap<Any> =
        MutableTypedClassMap(this)
            .apply { Unsafe.remove(classMapStringKey(key)) }
            .unsafeCast()

    companion object {
        @Suppress("FunctionName")
        @JvmStatic
        fun <Base: Any> Empty(): TypedClassMap<Base> = Empty.unsafeCast()
    }

    private object Empty: TypedClassMap<Any>(0) {
        override fun copy(): Empty = this
    }
}

/**
 * Creates a view of this [TypedClassMap] as a [ClassMap].
 * Both maps share the same underlying data, so changes in one will reflect in the other.
 */
fun TypedClassMap<*>.asClassMap(): ClassMap = MutableClassMap(Unsafe.data, Unsafe.json)

/**
 * Creates a new [ClassMap] with the same contents as this [TypedClassMap].
 * This is a copy conversion - modifications to the returned map will not affect the original.
 */
fun TypedClassMap<*>.toClassMap(): ClassMap = MutableClassMap(this)

/**
 * Creates a mutable copy of this [TypedClassMap].
 * Modifications to the returned map will not affect the original.
 */
fun <T: Any> TypedClassMap<T>.toMutableTypedClassMap(): MutableTypedClassMap<T> = MutableTypedClassMap(this)