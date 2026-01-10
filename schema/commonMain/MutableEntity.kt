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
    constructor(schema: S, other: DynoMap<SchemaProperty<S, *>>, readSafety: DynoReadSafety = other.unsafeCast<DynoMapImpl>().readSafety):
            super(schema, other, readSafety)
    @UnsafeDynoApi
    constructor(schema: S, data: MutableMap<Any, Any>?, json: Json?, readSafety: DynoReadSafety = DynoReadSafety.SYNCHRONIZED):
            super(schema, data, json, readSafety)

    override fun copy(): MutableEntity<S> = toMutableEntity()
}

/**
 * Returns a new [MutableEntity] containing all key-value pairs from the original [Entity].
 */
fun <S : DynoSchema> Entity<S>.toMutableEntity(): MutableEntity<S> = MutableEntity(
    schema,
    DynoMapBase.Unsafe.data?.let(::HashMap),
    DynoMapBase.Unsafe.json,
    readSafety
)