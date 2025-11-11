@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import karamel.utils.unsafeCast
import kotlin.internal.Exact


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

class DynoMapBuilder<S: AbstractDynoSchema<*>> @PublishedApi internal constructor(
    private val checker: EntityConsistensyChecker,
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
                "DynoMap of schema '${checker.schema.name()}' " +
                "missing the following properties: ${missing.joinToString(", ")}"
            )
        }

        return result.unsafeCast()
    }
}

/**
 * Sets the [value] for the given [this] key in the map.
 *
 * If [value] is `null`, the entry associated with [this] key is removed from the map.
 */
context(builder: DynoMapBuilder<S>)
infix fun <S: AbstractDynoSchema<*>, K: SchemaProperty<S, in T>, @Exact T> K.set(value: T?) {
    builder.set(this.unsafeCast(), value)
}