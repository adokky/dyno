package dyno

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * Represents a typed key for accessing values in a [DynoMap] or [DynamicObject].
 *
 * Keys are identified by their [name] and are associated with a specific type [T].
 * Keys with the same name but different types are considered equal and will conflict.
 *
 * ## Validation
 *
 * The [onAssign] and [onDecode] methods can be overridden to add validation logic.
 * - [onAssign] is called when a value is manually assigned to the key.
 * - [onDecode] is called when a value is deserialized from a JSON source.
 *
 * If validation fails, these methods should throw an exception.
 *
 * ## Example
 *
 * ```
 * val nameKey = DynoKey<String>("name")
 * val ageKey = DynoKey<Int>("age")
 * val person = dynamicObjectOf(nameKey with "Alex", ageKey with 30)
 *
 * val name: String = person[nameKey]
 * val age: Int = person[ageKey]
 * ```
 */
@OptIn(ExperimentalSerializationApi::class)
interface DynoKey<T: Any>: AbstractEagerDynoSerializer.ResolveResult {
    val name: String
    val serializer: KSerializer<T>

    /** Called when putting key manually. Useful for validation */
    fun onAssign(value: T): T = value

    /** Called on deserialization. Useful for validation */
    fun onDecode(value: T): T = value
}

/**
 * A [DynoKey] that marks the associated value as required.
 *
 * When using a [DynoRequiredKey], the regular [DynamicObject.get] method will behave
 * like [DynamicObject.getOrFail], throwing an exception if the key is not present.
 *
 * ## Example
 *
 * ```
 * val idKey = DynoRequiredKey<Int>("id")
 * val nameKey = DynoKey<String>("name")
 * val obj = dynamicObjectOf(idKey with 123, nameKey with "Alex")
 *
 * // Returns non-nullable type
 * val id: Int = obj[idKey]
 *
 * // Returns nullable type
 * val name: String? = obj[nameKey]
 * ```
 */
interface DynoRequiredKey<T: Any>: DynoKey<T>

// default constructors

/**
 * Creates a new [DynoKey] with the given [name] and [serializer].
 */
fun <T: Any> DynoKey(name: String, serializer: KSerializer<T>): DynoKey<T> =
    SimpleDynoKey(name, serializer)

/**
 * Creates a new [DynoRequiredKey] with the given [name] and [serializer].
 */
fun <T: Any> DynoRequiredKey(name: String, serializer: KSerializer<T>): DynoRequiredKey<T> =
    SimpleDynoRequiredKey(name, serializer)

/**
 * Creates a new [DynoKey] with [serializer] for the type [T] with the given [name].
 */
inline fun <reified T: Any> DynoKey(name: String): DynoKey<T> =
    DynoKey(name, serializer<T>())

/**
 * Creates a new [DynoRequiredKey] with [serializer] for the type [T] with the given [name].
 */
inline fun <reified T: Any> DynoRequiredKey(name: String): DynoRequiredKey<T> =
    DynoRequiredKey(name, serializer<T>())

/**
 * Creates a new [DynoKey] for serializable type [T] with its serial name as the key name.
 */
inline fun <reified T: Any> DynoKey(): DynoKey<T> {
    val serializer = serializer<T>()
    return DynoKey(serializer.descriptor.serialName, serializer)
}

/**
 * Creates a new [DynoKey] for serializable type [T] with its serial name as the key name.
 */
inline fun <reified T: Any> DynoRequiredKey(): DynoRequiredKey<T> {
    val serializer = serializer<T>()
    return DynoRequiredKey(serializer.descriptor.serialName, serializer)
}

/**
 * A basic implementation of [DynoKey].
 *
 * Keys are compared by their [name] only. This means that two keys with the same name
 * but different types will be considered equal:
 * ```
 * val key1 = SimpleDynoKey("count", Int.serializer())
 * val key2 = SimpleDynoKey("count", Long.serializer())
 * // key1 == key2 is true!
 * ```
 * It is the user's responsibility to ensure key names are unique across types if needed.
 * @see SimpleDynoRequiredKey
 */
open class SimpleDynoKey<T: Any>(
    final override val name: String,
    final override val serializer: KSerializer<T>
) : DynoKey<T>, Comparable<DynoKey<T>> {
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean =
        this === other || (other as? DynoKey<*>)?.name == name

    override fun hashCode(): Int = name.hashCode()

    override fun compareTo(other: DynoKey<T>): Int = name.compareTo(other.name)

    companion object {
        /**
         * Creates a new [SimpleDynoKey] with [serializer] for the type [T] the given [name].
         */
        @JvmName("get")
        @JvmStatic
        inline operator fun <reified T: Any> invoke(name: String): SimpleDynoKey<T> =
            SimpleDynoKey(name, serializer<T>())
    }
}

/**
 * A basic implementation of [DynoRequiredKey].
 *
 * Keys are compared by their [name] only. This means that two keys with the same name
 * but different types will be considered equal:
 * ```
 * val key1 = SimpleDynoKey("count", Int.serializer())
 * val key2 = SimpleDynoRequiredKey("count", Long.serializer())
 * // key1 == key2 is true!
 * ```
 * It is the user's responsibility to ensure key names are unique across types if needed.
 */
open class SimpleDynoRequiredKey<T: Any>(
    name: String,
    serializer: KSerializer<T>
) : SimpleDynoKey<T>(name, serializer), DynoRequiredKey<T> {
    companion object {
        /**
         * Creates a new [SimpleDynoRequiredKey] with [serializer] for the type [T] the given [name].
         */
        @JvmName("get")
        @JvmStatic
        inline operator fun <reified T: Any> invoke(name: String): SimpleDynoRequiredKey<T> =
            SimpleDynoRequiredKey(name, serializer<T>())
    }
}