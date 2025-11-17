package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer

open class SimpleDynoSchema internal constructor(
    private val name: String,
    private val version: Int,
    unknownKeysStrategy: UnknownKeysStrategy,
    keys: Collection<DynoKey<*>>
): AbstractDynoSchema<DynoMap<DynoKey<*>>>(keys = keys) {
    constructor(
        name: String,
        version: Int = 0,
        unknownKeysStrategy: UnknownKeysStrategy = PolymorphicDynoSerializer.DEFAULT_UNKNOWN_KEY_STRATEGY
    ): this(name = name, version = version, unknownKeysStrategy, keys = emptyList())

    constructor(
        other: DynoSchema,
        name: String = other.name(),
        version: Int = other.version(),
        unknownKeysStrategy: UnknownKeysStrategy =
            (other as? SimpleDynoSchema)?.serializer?.unknownKeysStrategy
                ?: PolymorphicDynoSerializer.DEFAULT_UNKNOWN_KEY_STRATEGY
    ): this(
        name = name,
        version = version,
        unknownKeysStrategy,
        keys = other.keys().unsafeCast()
    )

    private val serializer = SchemaSerializer<DynoMap<DynoKey<*>>>(this, unknownKeysStrategy) { data, json ->
        MutableDynoMap(data, json)
    }

    override fun serializer(): KSerializer<DynoMap<DynoKey<*>>> = serializer

    final override fun name(): String = name

    final override fun version(): Int = version
}


