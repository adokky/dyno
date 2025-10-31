package dyno

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder

/**
 * Registers a [DynamicObjectKeyRegistry] built via [KeyProviderRegistryBuilder] DSL
 * into the current [SerializersModuleBuilder].
 *
 * Example:
 * ```
 * SerializersModule {
 *     dynamicObjectKeys {
 *         keyProvider("schema1", MyKeyProvider1)
 *         keyProvider("schema2", MyKeyProvider2)
 *         defaultProvider = MyDefaultProvider
 *     }
 * }
 * ```
 */
fun SerializersModuleBuilder.dynamicObjectKeys(build: KeyProviderRegistryBuilder.() -> Unit) {
    dynamicObjectKeys(KeyProviderRegistryBuilder().apply(build).toRegistry())
}

/**
 * Builder for constructing a [DynamicObjectKeyRegistry].
 *
 * Allows registering [DynoKeyProvider] instances mapped by schema names,
 * and setting a default provider for fallback key resolution.
 */
class KeyProviderRegistryBuilder internal constructor() {
    internal val byId = HashMap<String, DynoKeyProvider>()

    /**
     * Default [DynoKeyProvider] used when no schema-specific provider is found.
     */
    var defaultProvider: DynoKeyProvider? = null

    /**
     * Registers a [DynoKeyProvider] for the given [schema].
     *
     * If another provider was already registered for the same schema,
     * they are chained: the old provider is consulted first, and the new one acts as fallback.
     */
    fun keyProvider(schema: String, provider: DynoKeyProvider) {
        var newProvider = provider

        val oldProvider = byId[schema]
        if (oldProvider != null) newProvider = object : DynoKeyProvider {
            override fun getDynoKey(serializersModule: SerializersModule, name: String): DynoKey<*>? =
                oldProvider.getDynoKey(serializersModule, name)
                    ?: provider.getDynoKey(serializersModule, name)

            override val name: String = schema
        }

        byId[schema] = newProvider
    }

    internal fun toRegistry(): DynamicObjectKeyRegistry = DynamicObjectKeyRegistry(
        schemaToProvider = { byId[it] },
        default = defaultProvider
    )
}