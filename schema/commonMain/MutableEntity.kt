package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.json.Json

/**
 * A mutable version of [Entity] that allows in-place modifications of the underlying map.
 *
 * Unlike [Entity], [MutableEntity] implements [MutableDynoMap], enabling runtime modifications
 * while still maintaining schema-bound type safety.
 *
 * Example:
 * ```
 * val person = MutableEntity(Person)
 * person[Person.name] = "Alex"
 * person[Person.age] = 30
 *
 * // Or using put
 * person.put(Person.name, "Alex")
 * ```
 *
 * Note: Mutations do not automatically trigger schema validation. Ensure all required fields
 * are present before serialization or further processing.
 *
 * @see Entity
 * @see MutableDynoMap
 */
class MutableEntity<out S: DynoSchema>: Entity<S>, MutableDynoMap<SchemaProperty<S, *>> {
    constructor(schema: S): super(schema)
    constructor(schema: S, capacity: Int): super(schema, capacity)
    constructor(schema: S, other: DynoMap<SchemaProperty<S, *>>, threadSafeRead: Boolean = other.unsafeCast<DynoMapImpl>().threadSafeRead):
            super(schema, other, threadSafeRead)
    @UnsafeDynoApi
    constructor(schema: S, data: MutableMap<Any, Any>?, json: Json?, threadSafeRead: Boolean = data != null && json != null):
            super(schema, data, json, threadSafeRead)
    internal constructor(schema: S, entries: Collection<DynoEntry<*, *>>): super(schema, entries)

    override fun copy(): MutableEntity<S> = MutableEntity(schema,
        DynoMapBase.Unsafe.data?.let(::HashMap),
        DynoMapBase.Unsafe.json,
        threadSafeRead
    )
}