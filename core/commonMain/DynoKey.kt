package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

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
interface DynoKey<T>: AbstractEagerDynoSerializer.ResolveResult {
    val name: String
    val serializer: KSerializer<T & Any>

    /** Called when putting key manually. Useful for validation */
    val onAssign: DynoKeyProcessor<T & Any>? get() = null

    /** Called on deserialization. Useful for validation */
    val onDecode: DynoKeyProcessor<T & Any>? get() = null
}

// default constructors

/**
 * Creates a new [DynoKey] with the given [name] and [serializer].
 */
fun <T: Any> DynoKey(name: String, serializer: KSerializer<T>): DynoKey<T> =
    SimpleDynoKey(name, serializer)

/**
 * Creates a new [DynoKey] with [serializer] for the type [T] with the given [name].
 */
inline fun <reified T> DynoKey(name: String): DynoKey<T> =
    SimpleDynoKey(name, serializer<T>().unsafeCast())