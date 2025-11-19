package dyno

import kotlinx.serialization.json.Json

/**
 * Represents a type-safe wrapper over [DynoMap] that is bound to a specific [DynoSchema].
 *
 * An [Entity] ensures that all operations on the underlying map conform to the schema's structure,
 * providing compile-time safety and automatic validation during deserialization.
 *
 * Entities are immutable by default. For mutable operations, use [MutableEntity].
 *
 * Example:
 * ```
 * // Define schema
 * object Person: EntitySchema("person") {
 *     val name by dynoKey<String>()
 *     val age by dynoKey<Int>()
 * }
 *
 * // Create entity
 * val person = Person.new {
 *     name set "Alex"
 *     age set 30
 * }
 *
 * // Access values in a type-safe way
 * println("Name: ${person[Person.name]}, Age: ${person[Person.age]}")
 * ```
 *
 * @see MutableEntity
 */
sealed class Entity<out S: DynoSchema>: DynoMapImpl, DynoMap<SchemaProperty<S, *>> {
    val schema: S

    constructor(schema: S): super() {
        this.schema = schema
    }
    constructor(schema: S, capacity: Int): super(capacity) {
        this.schema = schema
    }
    constructor(schema: S, entries: Collection<DynoEntry<*, *>>): super(entries) {
        this.schema = schema
    }
    constructor(schema: S, other: DynoMap<SchemaProperty<S, *>>): super(other) {
        this.schema = schema
    }
    constructor(schema: S, data: MutableMap<Any, Any>?, json: Json?): super(data, json) {
        this.schema = schema
    }

    override abstract fun copy(): Entity<S>
}

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
    constructor(schema: S, other: DynoMap<SchemaProperty<S, *>>): super(schema, other)
    internal constructor(schema: S, entries: Collection<DynoEntry<*, *>>): super(schema, entries)
    @UnsafeDynoApi
    constructor(schema: S, data: MutableMap<Any, Any>?, json: Json?): super(schema, data, json)

    override fun copy(): MutableEntity<S> = MutableEntity(schema,
        DynoMapBase.Unsafe.data?.let(::HashMap),
        DynoMapBase.Unsafe.json
    )
}