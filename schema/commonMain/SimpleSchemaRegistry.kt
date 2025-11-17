package dyno

sealed interface DynoSchemaRegistry

/**
 * Registry holding a mapping from schema IDs to [DynoSchema]s.
 *
 * Used by [PolymorphicDynoSerializer] during deserialization.
 */
sealed interface SimpleSchemaRegistry: DynoSchemaRegistry {
    val all: Sequence<DynoSchema>

    val fallback: DynoSchema?

    fun get(name: String, version: Int = -1): DynoSchema?
}

sealed interface MutableSimpleSchemaRegistry: SimpleSchemaRegistry {
    override var fallback: DynoSchema?

    fun register(schema: DynoSchema)

    fun overwrite(schema: DynoSchema): DynoSchema?

    fun remove(name: String, version: Int): DynoSchema?
}



