package dyno

sealed interface PolymorhicSchemaRegistry: DynoSchemaRegistry {
    val global: SimpleSchemaRegistry // can also be retrieved as base schema with empty name

    val allLatest: Sequence<BaseSchemaRegistry>

    class BaseSchemaRegistry(val name: String, val version: Int, val subSchemas: SimpleSchemaRegistry)

    /** @param version if < 0 then latest version is returned */
    fun get(name: String, version: Int = -1): SimpleSchemaRegistry?
}

sealed interface MutablePolymorhicSchemaRegistry: PolymorhicSchemaRegistry {
    fun overwrite(name: String, version: Int, subSchemas: SimpleSchemaRegistry): SimpleSchemaRegistry?

    fun remove(name: String, version: Int): SimpleSchemaRegistry?
}