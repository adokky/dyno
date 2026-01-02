package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer

open class DynoMapSchema internal constructor(
    private val name: String,
    private val version: Int,
    unknownKeysStrategy: UnknownKeysStrategy,
    keys: Collection<DynoKey<*>>,
    private val threadSafeRead: Boolean
): AbstractDynoSchema<DynoMap<DynoKey<*>>>(keys = keys) {
    constructor(
        name: String,
        version: Int = 0,
        unknownKeysStrategy: UnknownKeysStrategy = PolymorphicDynoSerializer.DEFAULT_UNKNOWN_KEY_STRATEGY,
        threadSafeRead: Boolean = true
    ): this(
        name = name,
        version = version,
        unknownKeysStrategy,
        keys = emptyList(),
        threadSafeRead = threadSafeRead
    )

    constructor(
        other: DynoSchema,
        name: String = other.name(),
        version: Int = other.version(),
        unknownKeysStrategy: UnknownKeysStrategy =
            (other as? DynoMapSchema)?.serializer?.unknownKeysStrategy
                ?: PolymorphicDynoSerializer.DEFAULT_UNKNOWN_KEY_STRATEGY,
        threadSafeRead: Boolean = true
    ): this(
        name = name,
        version = version,
        unknownKeysStrategy,
        keys = other.keys().unsafeCast(),
        threadSafeRead = threadSafeRead
    )

    private val serializer = SchemaSerializer<DynoMap<DynoKey<*>>>(this, unknownKeysStrategy) { data, json ->
        MutableDynoMap(data, json, threadSafeRead)
    }

    override fun getSerializer(): KSerializer<DynoMap<DynoKey<*>>> = serializer

    final override fun name(): String = name

    final override fun version(): Int = version
}


