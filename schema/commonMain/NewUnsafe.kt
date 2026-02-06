@file:Suppress("INVISIBLE_REFERENCE")

package dev.dokky.dyno

import kotlin.internal.Exact

/**
 * Creates a new [DynoMap] instance with the specified [capacity] and initializes it using the provided [body] block.
 *
 * This function allows for direct creation and initialization of a [DynoMap] without the overhead of validation
 * or safety checks. It's designed for cases where the caller is certain about the correctness of the data
 * being added and wants to avoid unnecessary runtime checks.
 *
 * Example:
 * ```
 * val person = Person.newUnsafe {
 *     name set "Alex"
 *     age set 30
 * }
 * ```
 *
 * @param capacity The initial capacity of the map. Defaults to the number of keys in the schema.
 * @param body A block that initializes the map. The receiver of this block is a [MutableDynoMap], allowing direct assignment to keys.
 */
inline fun <S: AbstractDynoSchema<DynoMap<K>>, K: DynoKey<*>> S.newUnsafe(
    capacity: Int = keyCount(),
    body: context(MutableDynoMap<K>) S.() -> Unit
): DynoMap<K> {
    val map = MutableDynoMap<K>(capacity)
    with(map) {
        body()
    }
    return map
}

/**
 * Creates a new [Entity] instance with the specified [capacity] and initializes it using the provided [body] block.
 *
 * This function allows for direct creation and initialization of an [Entity] without the overhead of validation
 * or safety checks. It's designed for cases where the caller is certain about the correctness of the data
 * being added and wants to avoid unnecessary runtime checks.
 *
 * Example:
 * ```
 * val vehicle = Car.newUnsafe {
 *     name set "Toyota"
 *     wheels set 4
 * }
 * ```
 *
 * @param capacity The initial capacity of the entity. Defaults to the number of keys in the schema.
 * @param body A block that initializes the entity. The receiver of this block is a [MutableEntity], allowing direct assignment to keys.
 */
inline fun <S: EntitySchema> S.newUnsafe(
    capacity: Int = keyCount(),
    body: context(MutableEntity<S>) S.() -> Unit
): Entity<S> = newMutableUnsafe(capacity, body)

/**
 * Creates a new [MutableEntity] instance with the specified [capacity] and initializes it using the provided [body] block.
 *
 * This function allows for direct creation and initialization of an [Entity] without the overhead of validation
 * or safety checks. Use cases:
 * - Partial initialization. When you need to create an entity with only some of its properties set,
 * bypassing validation for required fields that are not yet available;
 * - Performance optimization. The caller is certain about the correctness of the data
 * being added and wants to avoid unnecessary runtime checks.
 *
 * Example:
 * ```
 * val partialEntity = Car.newMutableUnsafe {
 *     name set "Toyota"
 *     // wheels is not set yet, but entity is still created
 * }
 * ```
 *
 * @param capacity The initial capacity of the entity. Defaults to the number of keys in the schema.
 * @param body A block that initializes the entity. The receiver of this block is a [MutableEntity], allowing direct assignment to keys.
 */
inline fun <S: EntitySchema> S.newMutableUnsafe(
    capacity: Int = keyCount(),
    body: context(MutableEntity<S>) S.() -> Unit
): MutableEntity<S> {
    val map = MutableEntity<S>(this, capacity)
    with(map) {
        body()
    }
    return map
}

context(entity: MutableEntity<S>)
infix fun <S: DynoSchema, T> EntityProperty<S, in T>.set(value: @Exact T) {
    entity[this] = value
}