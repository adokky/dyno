package dev.dokky.dyno

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
    constructor(schema: S, other: DynoMap<SchemaProperty<S, *>>, readSafety: DynoReadSafety): super(other, readSafety) {
        this.schema = schema
    }
    constructor(schema: S, data: MutableMap<Any, Any>?, json: Json?, readSafety: DynoReadSafety): super(data, json, readSafety) {
        this.schema = schema
    }

    /**
     * Returns a new [Entity] containing all key-value pairs from the original entity.
     */
    override abstract fun copy(): Entity<S>
}