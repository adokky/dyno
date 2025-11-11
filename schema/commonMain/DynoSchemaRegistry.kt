package dyno

sealed interface DynoSchemaRegistry

/**
 * Registry holding a mapping from schema IDs to [DynoSchema]s.
 *
 * Used by [PolymorphicDynoSerializer] during deserialization.
 */
interface DynoSimpleSchemaRegistry: DynoSchemaRegistry {
    val all: Collection<DynoSchema>

    fun getSchema(name: String?): DynoSchema?
}

interface DynoPolymorhicSchemaRegistry: DynoSchemaRegistry {
    fun getSubRegistry(baseSchemaName: String): DynoSimpleSchemaRegistry?

    fun getSchema(baseSchemaName: String, name: String?): DynoSchema?
}