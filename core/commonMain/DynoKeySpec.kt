package dyno

@SubclassOptInRequired(UnsafeDynoApi::class)
interface DynoKeySpec<T: Any> {
    /** See [DynoKey.onAssign] */
    val Internal.onAssign: DynoKeyProcessor<T>?

    /** See [DynoKey.onDecode] */
    val Internal.onDecode: DynoKeyProcessor<T>?

    /**
     * Creates a new instance of [DynoKeySpec] with the specified parameters.
     *
     * WARNING: Implementers **must** return a copy of the same subtype, not a different [DynoKeySpec] implementation.
     * Otherwise, using [onDecode], [onAssign], [validate] may result in [ClassCastException].
     *
     * @return A new instance of the same [DynoKeySpec] subtype with updated parameters
     */
    fun Internal.copy(
        onAssign: DynoKeyProcessor<T>? = Internal.onAssign,
        onDecode: DynoKeyProcessor<T>? = Internal.onDecode
    ): DynoKeySpec<T>

    @ExperimentalDynoApi
    object Internal
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
 * val email by dynoKey<String>()
 *     .onDecode { value ->
 *         require('@' in value) { "Invalid email format: $value" }
 *     }
 * ```
 */
@Suppress("UNCHECKED_CAST")
fun <R: DynoKeySpec<T>, T: Any> R.onDecode(processor: DynoKeyProcessor<T>): R =
    with(DynoKeySpec.Internal) { copy(onDecode = onDecode + processor) as R }

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
 * val positiveInt by dynoKey<Int>()
 *     .onAssign { value ->
 *         require(value > 0) { "Value must be positive, but was: $value" }
 *     }
 * ```
 */
@Suppress("UNCHECKED_CAST")
fun <R: DynoKeySpec<T>, T: Any> R.onAssign(processor: DynoKeyProcessor<T>): R =
    with(DynoKeySpec.Internal) { copy(onAssign = onAssign + processor) as R }

/**
 * Adds validation logic that is applied to both manually assigned values and deserialized values.
 * The validation body is assigned to both [onAssign] and [onDecode] callbacks.
 *
 * Use [onDecode] alone when validation is only needed for deserialized objects
 * received from network.
 *
 * All processors are chained in the order of assignment.
 */
@Suppress("UNCHECKED_CAST")
fun <R: DynoKeySpec<T>, T: Any> R.validate(processor: DynoKeyProcessor<T>): R =
    with(DynoKeySpec.Internal) {
        copy(
            onDecode = onDecode + processor,
            onAssign = onAssign + processor
        ) as R
    }