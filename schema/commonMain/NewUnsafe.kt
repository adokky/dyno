@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import kotlin.internal.Exact

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

inline fun <S: EntitySchema> S.newUnsafe(
    capacity: Int = keyCount(),
    body: context(MutableEntity<S>) S.() -> Unit
): Entity<S> {
    val map = MutableEntity<S>(this, capacity)
    with(map) {
        body()
    }
    return map
}

context(entity: MutableEntity<S>)
infix fun <S: DynoSchema, T> SchemaProperty<S, in T>.set(value: @Exact T) {
    entity[this] = value
}