@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import kotlinx.serialization.Serializable
import kotlin.internal.Exact

@Serializable(MutableEntitySerializer::class)
interface MutableEntity<S : TypedDynoSchema<S>>: Entity<S>, MutableDynoMap<SchemaProperty<S, *>> {
    override fun copy(): MutableEntity<S>

    operator fun <T> set(key: SchemaProperty<S, in T>, value: @Exact T)

    fun <T> put(key: SchemaProperty<S, T>, value: @Exact T): T?

    fun <T> remove(key: SchemaProperty<S, T>): @Exact T?

    fun set(entry: EnityEntry<S>)

    fun <T> put(entry: DynoEntry<SchemaProperty<S, T & Any>, @Exact T>): T?

    fun putAll(entries: Iterable<DynoEntry<SchemaProperty<S, *>, *>>)

    operator fun plusAssign(entry: DynoEntry<SchemaProperty<S, *>, *>)

    operator fun minusAssign(key: SchemaProperty<S, *>)
}
