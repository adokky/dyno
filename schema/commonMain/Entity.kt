package dyno

import kotlinx.serialization.json.Json

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

class MutableEntity<out S: DynoSchema>: Entity<S>, MutableDynoMap<SchemaProperty<S, *>> {
    constructor(schema: S): super(schema)
    constructor(schema: S, capacity: Int): super(schema, capacity)
    constructor(schema: S, entries: Collection<DynoEntry<*, *>>): super(schema, entries)
    constructor(schema: S, other: DynoMap<SchemaProperty<S, *>>): super(schema, other)
    @UnsafeDynoApi
    constructor(schema: S, data: MutableMap<Any, Any>?, json: Json?): super(schema, data, json)

    override fun copy(): MutableEntity<S> = MutableEntity(schema,
        DynoMapBase.Unsafe.data?.let(::HashMap),
        DynoMapBase.Unsafe.json
    )
}