@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import karamel.utils.unsafeCast
import kotlin.internal.Exact

/**
 * Creates a new [DynoMap] instance using the schema's structure and enforces validation of required fields.
 *
 * This function provides a type-safe way to construct a [DynoMap] by ensuring all required keys defined in the schema
 * are set during creation. It uses a DSL builder pattern where each key can be assigned a value using the `set` infix function.
 *
 * Example:
 * ```
 * val person = Person.new {
 *     name set "Alex"
 *     age set 30
 * }
 * ```
 *
 * @throws IllegalStateException if any required property is missing after the builder block execution.
 *
 * @see AbstractDynoSchema
 * @see SchemaProperty.set
 */
inline fun <S: AbstractDynoSchema<out DynoMap<K>>, K: DynoKey<*>> S.new(
    capacity: Int = keyCount(),
    body: context(DynoMapBuilder<S>) S.() -> Unit
): DynoMap<K> {
    val result = MutableDynoMap<K>(capacity)
    val map = DynoMapBuilder<S>(checker, result)
    with(map) {
        body()
    }
    return map.getResult()
}

/**
 * Creates a new [Entity] instance using the schema's structure and enforces validation of required fields.
 *
 * Similar to [new] for [DynoMap], this function ensures all required keys are set during entity creation.
 * The resulting [Entity] is bound to the schema and provides type-safe access to its properties.
 *
 * Example:
 * ```
 * val vehicle = Car.new {
 *     name set "Toyota"
 *     wheels set 4
 * }
 * ```
 *
 * @throws IllegalStateException if any required property is missing after the builder block execution.
 *
 * @see EntitySchema
 * @see SchemaProperty.set
 */
inline fun <S: EntitySchema> S.new(
    capacity: Int = keyCount(),
    body: context(DynoMapBuilder<S>) S.() -> Unit
): Entity<S> {
    val result = MutableEntity<S>(this, capacity)
    val map = DynoMapBuilder<S>(checker, result)
    with(map) {
        body()
    }
    return map.getResult()
}

/**
 * A builder class used to construct [DynoMap] or [Entity] instances with validation of required fields.
 *
 * This class tracks which required keys have been set during the building process and throws an exception
 * if any are missing when the build is finalized.
 */
class DynoMapBuilder<S: AbstractDynoSchema<*>> @PublishedApi internal constructor(
    private val checker: EntityConsistencyChecker,
    result: MutableDynoMap<*>
) {
    private val result: MutableDynoMap<DynoKey<Any?>> = result.unsafeCast()
    private var checkState: Any? = null

    internal fun set(key: DynoKey<in Any?>, value: Any?) {
        result[key] = value
        checkState = checker.markIsPresent(checkState, key)
    }

    @PublishedApi
    internal fun <R: DynoMap<*>> getResult(): R {
        val missing = checker.getRequiredKeysMissing(checkState)

        if (!missing.isNullOrEmpty()) {
            error(
                "Schema '${checker.schema.name()}' " +
                "requires the following properties: ${missing.joinToString(", ")}"
            )
        }

        return result.unsafeCast()
    }
}

/**
 * Sets the [value] for the given [this] key in the map being built.
 *
 * This infix function is used inside the builder block of [new] to assign values to keys.
 * If [value] is `null`, the entry associated with [this] key is removed from the map.
 *
 * Example:
 * ```
 * Person.new {
 *     name set "Alex"  // Sets the 'name' key to "Alex"
 *     age set null    // Removes the 'age' key if it was previously set
 * }
 * ```
 *
 * @see DynoMapBuilder
 * @see SchemaProperty
 */
context(builder: DynoMapBuilder<S>)
infix fun <S: AbstractDynoSchema<*>, K: SchemaProperty<S, in T>, @Exact T> K.set(value: T?) {
    builder.set(this.unsafeCast(), value)
}