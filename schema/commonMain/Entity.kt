@file:Suppress("INVISIBLE_REFERENCE")

package dyno

import kotlinx.serialization.Serializable
import kotlin.internal.Exact

// todo dynoPolymorphic<Person> { subclass(Employee) }

@Serializable(EntitySerializer::class)
interface Entity<S : TypedDynoSchema<S>>: DynoMap<SchemaProperty<S, *>> {
    override fun copy(): Entity<S>

    operator fun <T> get(key: SchemaProperty<S, @Exact T>): T?

    fun <T> getOrFail(key: SchemaProperty<S, @Exact T>): T & Any

    operator fun contains(key: SchemaProperty<S, *>): Boolean

    operator fun plus(entry: EnityEntry<S>): Entity<S>

    operator fun minus(key: SchemaProperty<S, *>): Entity<S>
}

