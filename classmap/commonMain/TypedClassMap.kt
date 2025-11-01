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
    protected constructor(other: ClassMap): super(other)
    constructor(data: MutableMap<Any, Any>?, json: Json?): super(data, json)

    abstract override fun copy(): TypedClassMap<Base>

    /**
     * Returns a new [TypedClassMap] containing all elements of this map and the given [value].
     * The [value]'s class is used as the key.
     */
    @JvmName("plusClassInstance")
    inline operator fun <reified T: Base> plus(value: T): TypedClassMap<Base> =
        MutableTypedClassMap(this)
            .apply { Unsafe.put(dynoKey<T>(), value) }
            .unsafeCast()

    /**
     * Returns a new [TypedClassMap] containing all elements of this map except the entry with the [key].
     */
    override fun <T : Any> minus(key: KClass<out T>): TypedClassMap<Any> =
        MutableTypedClassMap(this)
            .apply { Unsafe.remove(classMapStringKey(key)) }
            .unsafeCast()

    override fun <T : Any> minus(key: KType): TypedClassMap<Any> =
        MutableTypedClassMap(this)
            .apply { Unsafe.remove(classMapStringKey(key)) }
            .unsafeCast()

    companion object {
        @JvmStatic
        fun <Base: Any> Empty(): TypedClassMap<Base> = EmptyTypedClassMap.unsafeCast()
    }
}

private object EmptyTypedClassMap: TypedClassMap<Any>(0) {
    override fun copy(): EmptyTypedClassMap = this
}

fun TypedClassMap<*>.asClassMap(): ClassMap = MutableClassMap(Unsafe.data, Unsafe.json)

fun TypedClassMap<*>.toClassMap(): ClassMap = MutableClassMap(this)

fun <T: Any> TypedClassMap<T>.toMutableTypedClassMap(): MutableTypedClassMap<T> = MutableTypedClassMap(this)