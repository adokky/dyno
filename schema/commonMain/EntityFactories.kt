@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import kotlin.internal.Exact
import kotlin.jvm.JvmName

inline fun <S: TypedDynoSchema<S>> S.newMutable(
    capacity: Int = withMeta { ketCount },
    body: context(MutableEntity<S>) S.() -> Unit
): MutableEntity<S> {
    val entity = EntityImpl<S>(capacity)
    with(entity) { body() }
    return entity
}

inline fun <S: TypedDynoSchema<S>> S.new(
    capacity: Int = withMeta { ketCount },
    body: context(MutableEntity<S>) S.() -> Unit
): Entity<S> = newMutable(capacity, body)

context(entity: MutableEntity<S>)
infix fun <S: TypedDynoSchema<S>, T> SchemaProperty<S, in T>.set(value: @Exact T) {
    entity[this] = value
}

// resolves ambiguity in case then `null` literal is passed
@JvmName("setNull")
context(entity: MutableEntity<S>)
infix fun <S: TypedDynoSchema<S>> SchemaProperty<S, *>.set(value: Nothing?) {
    entity.remove(this)
}