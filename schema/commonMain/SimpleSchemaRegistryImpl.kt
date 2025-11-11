package dyno

internal open class SimpleSchemaRegistryImpl(val parent: PolymorphicSchemaRegistryImpl?): MutableSimpleSchemaRegistry {
    private val registry = VersionedRegistry<DynoSchema>()

    override var fallback: DynoSchema? = null
        set(value) {
            if (field != null) error("attempt to declare fallback schema twice. Old: '$field', new: '$value'")
            field = value
        }

    override val all: Sequence<DynoSchema> get() =
        registry.nameToVersions.values.asSequence().map { it.latest }

    override fun get(name: String, version: Int): DynoSchema? {
        return registry.get(name, version)
    }

    override fun register(schema: DynoSchema) {
        checkSchema(schema)

        if (!registry.tryPut(schema.name(), schema.version(), schema.withChecker())) {
            error("attempt to declare schema '${schema.name()}' twice")
        }
    }

    override fun overwrite(schema: DynoSchema): DynoSchema? {
        checkSchema(schema)
        return registry.overwrite(schema.name(), schema.version(), schema.withChecker())
    }

    override fun remove(name: String, version: Int): DynoSchema? {
        return registry.remove(name, version)
    }

    private fun checkSchema(schema: DynoSchema) {
        if (parent != null && schema is Polymorphic) {
            // Nested polymorphic schemas are not currently supported.
            // To support them, we would need either:
            // 1. Lazy resolution that delays error messages to runtime
            // 2. A special method like `resolveNestedPolymorphicSchemas()`
            //    on the top registry that developer would
            //    call after all schemas are registered
            TODO("nested polymorphic subclasses are not supported")
        }
    }
}

