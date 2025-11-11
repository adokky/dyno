package dyno

internal open class BaseSchemaRegistry: DynoSimpleSchemaRegistry {
    private val nameToSchema = HashMap<String, DynoSchema>()

    var defaultSchema: DynoSchema? = null
        set(value) {
            if (field != null) error("attempt to declare default schema twice. Old: '$field', new: '$value'")
            field = value
        }

    fun register(schema: DynoSchema) {
        if (schema.name() in nameToSchema) {
            error("attempt to declare schema '${schema.name()}' twice")
        }
        overwrite(schema)
    }

    fun overwrite(schema: DynoSchema) {
        nameToSchema[schema.name()] = schema
    }

    fun getSchemaOrNull(name: String): DynoSchema? = nameToSchema[name]

    override val all: Collection<DynoSchema> get() = nameToSchema.values

    override fun getSchema(name: String?): DynoSchema? =
        if (name == null) defaultSchema else getSchemaOrNull(name)
}

internal class DynoSchemaRegistryImpl: BaseSchemaRegistry(), DynoPolymorhicSchemaRegistry {
    private val baseToRegistry = HashMap<String, BaseSchemaRegistry>()

    override fun getSubRegistry(baseSchemaName: String): BaseSchemaRegistry? =
        baseToRegistry[baseSchemaName]

    fun overwriteBaseSchemaRegistry(baseName: String, registry: BaseSchemaRegistry) {
        baseToRegistry[baseName] = registry
    }

    override fun getSchema(baseSchemaName: String, name: String?): DynoSchema? =
        baseToRegistry[baseSchemaName]?.getSchema(name) ?: defaultSchema
}