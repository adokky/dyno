package dyno

sealed interface DynoSchemaRegistry

/**
 * Registry holding a mapping from schema IDs to [DynoSchema]s.
 *
 * Used by [PolymorphicDynoSerializer] during deserialization.
 */
sealed interface SimpleSchemaRegistry: DynoSchemaRegistry {
    val allLatest: Sequence<DynoSchema>

    val fallback: DynoSchema?

    /** @param version if < 0 then latest version is returned */
    fun get(name: String, version: Int = -1): DynoSchema?
}

sealed interface MutableSimpleSchemaRegistry: SimpleSchemaRegistry {
    override var fallback: DynoSchema?

    fun register(schema: DynoSchema)

    fun overwrite(schema: DynoSchema): DynoSchema?

    fun remove(name: String, version: Int): DynoSchema?
}



