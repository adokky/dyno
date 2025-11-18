package dyno

import kotlinx.serialization.modules.SerializersModuleBuilder

/**
 * Registers a [SimpleSchemaRegistry] built via [SchemaRegistryBuilder] DSL
 * into the current [SerializersModuleBuilder].
 *
 * Example:
 * ```
 * SerializersModule {
 *     dynoSchemaRegistry {
 *         schema(MySchema1)
 *         schema(MySchema2)
 *         fallbackSchema = MyDefaultSchema
 *
 *         polymorphic("baseSchema") {
 *             schema(SubSchema1)
 *             schema(SubSchema2)
 *             fallbackSchema = MySubSchema
 *         }
 *     }
 * }
 * ```
 */
fun SerializersModuleBuilder.dynoSchemaRegistry(build: SchemaRegistryBuilder.() -> Unit) {
    dynoSchemaRegistry(SchemaRegistryBuilder().apply(build).toRegistry())
}

/**
 * Builder for constructing a [SimpleSchemaRegistry].
 *
 * Allows registering [DynoSchema] instances mapped by schema names,
 * and setting a default schema as fallback.
 */
@DynoDslMarker
sealed class AbstractSchemaRegistryBuilder {
    internal abstract val topRegistry: MutableSimpleSchemaRegistry

    /**
     * Default [DynoSchema] used when schema is not found by name.
     */
    var fallback: DynoSchema?
        get() = topRegistry.fallback
        set(value) { topRegistry.fallback = value }

    fun schema(schema: DynoSchema) {
        topRegistry.register(schema)
    }

    /**
     * Registers a [DynoSchema] for the given [schema].
     *
     * If another schema was already registered for the same name,
     * they are merged using [combine].
     */
    fun extendSchema(schema: DynoSchema) {
        var newSchema = schema

        val oldSchema = topRegistry.get(schema.name(), 0)
        if (oldSchema != null) newSchema = oldSchema.combine(schema)

        topRegistry.overwrite(newSchema)
    }
}

@DynoDslMarker
class SchemaRegistryBuilder internal constructor(
    registry: PolymorphicSchemaRegistryImpl = PolymorphicSchemaRegistryImpl()
): AbstractSchemaRegistryBuilder() {
    private var _registry: PolymorphicSchemaRegistryImpl? = registry
    internal val registry: PolymorphicSchemaRegistryImpl
        get() = _registry ?: error("SchemaRegistryBuilder was used once")

    override val topRegistry get() = registry.global

    fun polymorhic(baseName: String, version: Int = 0, body: PolymorphicSchemaRegistryBuilder.() -> Unit) {
        val old = registry.get(baseName, version) ?: SimpleSchemaRegistryImpl(registry)
        val baseRegistry = PolymorphicSchemaRegistryBuilder(old).apply(body).toRegistry()
        registry.overwrite(baseName, version, baseRegistry)
    }

    fun polymorhic(baseSchema: DynoSchema, body: PolymorphicSchemaRegistryBuilder.() -> Unit) {
        polymorhic(baseSchema.name(), baseSchema.version(), body)
    }

    internal fun toRegistry(): MutablePolymorhicSchemaRegistry = registry.also { _registry = null }
}

@DynoDslMarker
class PolymorphicSchemaRegistryBuilder internal constructor(
    registry: MutableSimpleSchemaRegistry
): AbstractSchemaRegistryBuilder() {
    private var _registry: MutableSimpleSchemaRegistry? = registry
    override val topRegistry: MutableSimpleSchemaRegistry
        get() = _registry ?: error("PolymorphicSchemaRegistryBuilder was used once")

    internal fun toRegistry(): MutableSimpleSchemaRegistry = topRegistry.also { _registry = null }
}