package dyno

import karamel.utils.unsafeCast

internal class PolymorphicSchemaRegistryImpl: MutablePolymorhicSchemaRegistry {
    private val registry = VersionedRegistry<SimpleSchemaRegistryImpl>()

    override val allLatest: Sequence<PolymorhicSchemaRegistry.BaseSchemaRegistry>
        get() = registry.nameToVersions.entries.asSequence().map {
            PolymorhicSchemaRegistry.BaseSchemaRegistry(
                name = it.key,
                version =  it.value.version,
                it.value.latest
            )
        }

    override val global = SimpleSchemaRegistryImpl(null)

    init {
        overwrite("", 0, global)
    }

    override operator fun get(name: String, version: Int): SimpleSchemaRegistryImpl? =
        registry.get(name, version)

    override fun overwrite(
        name: String,
        version: Int,
        subSchemas: SimpleSchemaRegistry
    ): SimpleSchemaRegistry? =
        registry.overwrite(name, version, subSchemas.unsafeCast())

    override fun remove(name: String, version: Int): SimpleSchemaRegistryImpl? =
        registry.remove(name, version)
}

