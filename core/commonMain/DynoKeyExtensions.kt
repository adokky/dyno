package dyno

import kotlinx.serialization.KSerializer

/**
 * Adds [DynoKeyProcessor] that is called when a value is manually assigned to this key.
 * This includes any programmatic assignments like ```obj.put(key, value)``` or `dynamicObjectOf(key with value)`.
 *
 * Useful for validation or transformation of values before they are stored.
 *
 * All processors are chained in the order of assignment.
 *
 * Example:
 * ```
 * val positiveIntKey = DynoKey<Int>("positiveInt")
 *     .onAssign { value ->
 *         require(value > 0) { "Value must be positive, but was: $value" }
 *     }
 * ```
 */
fun <T> DynoKey<T>.onDecode(processor: DynoKeyProcessor<T & Any>): DynoKey<T> {
    val newProcessor = onDecode + processor
    return when {
        this::class == SimpleDynoKey::class -> SimpleDynoKey(name, serializer,
            onAssign = onAssign, onDecode = newProcessor)
        else -> DynoKeyDelegate(this@onDecode, onDecode = newProcessor)
    }
}

/**
 * Adds [DynoKeyProcessor] that is called when a value is deserialized from external source.
 * This includes JSON deserialization or other decoding operations.
 *
 * Useful for validation of incoming data or transformation of deserialized values.
 *
 * All processors are chained in the order of assignment.
 *
 * Example:
 * ```
 * val emailKey = DynoKey<String>("email")
 *     .onDecode { value ->
 *         require('@' in value) { "Invalid email format: $value" }
 *     }
 * ```
 */
fun <T> DynoKey<T>.onAssign(processor: DynoKeyProcessor<T & Any>): DynoKey<T> {
    val newProcessor = onAssign + processor
    return when {
        this::class == SimpleDynoKey::class -> SimpleDynoKey(name, serializer,
            onAssign = newProcessor, onDecode = onDecode)
        else -> DynoKeyDelegate(this@onAssign, onAssign = newProcessor)
    }
}

/**
 * Adds validation logic that is applied to both manually assigned values and deserialized values.
 * The validation body is assigned to both [onAssign] and [onDecode] callbacks.
 *
 * Use [onDecode] alone when validation is only needed for deserialized objects
 * received from network.
 *
 * All processors are chained in the order of assignment.
 */
fun <T> DynoKey<T>.validate(processor: DynoKeyProcessor<T & Any>): DynoKey<T> {
    val newOnDecode = onDecode + processor
    val newOnAssign = onAssign + processor

    return when {
        this::class == SimpleDynoKey::class -> SimpleDynoKey(name, serializer,
            onAssign = newOnAssign, onDecode = newOnDecode)
        else -> DynoKeyDelegate(this@validate,
            onAssign = newOnAssign, onDecode = newOnDecode)
    }
}

@PublishedApi
internal class DynoKeyDelegate<T>(
    private val original: DynoKey<T>,
    override val name: String = original.name,
    override val serializer: KSerializer<T & Any> = original.serializer,
    override val onAssign: DynoKeyProcessor<T & Any>? = original.onAssign,
    override val onDecode: DynoKeyProcessor<T & Any>? = original.onDecode
) : DynoKey<T> {
    override fun equals(other: Any?): Boolean = original.equals(other)
    override fun hashCode(): Int = original.hashCode()
    override fun toString(): String = original.toString()
}