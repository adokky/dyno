package dev.dokky.dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer

open class DynoMapSchema internal constructor(
    private val name: String,
    private val version: Int,
    unknownKeysStrategy: UnknownKeysStrategy,
    keys: Collection<DynoKey<*>>,
    private val readSafety: DynoReadSafety
): AbstractDynoSchema<DynoMap<DynoKey<*>>>(keys = keys) {
    constructor(
        name: String,
        version: Int = 0,
        unknownKeysStrategy: UnknownKeysStrategy = PolymorphicDynoSerializer.DEFAULT_UNKNOWN_KEY_STRATEGY,
        readSafety: DynoReadSafety = DynoReadSafety.SYNCHRONIZED
    ): this(
        name = name,
        version = version,
        unknownKeysStrategy,
        keys = emptyList(),
        readSafety = readSafety
    )

    constructor(
        other: DynoSchema,
        name: String = other.name(),
        version: Int = other.version(),
        unknownKeysStrategy: UnknownKeysStrategy =
            (other as? DynoMapSchema)?.serializer?.unknownKeysStrategy
                ?: PolymorphicDynoSerializer.DEFAULT_UNKNOWN_KEY_STRATEGY,
        readSafety: DynoReadSafety = DynoReadSafety.SYNCHRONIZED
    ): this(
        name = name,
        version = version,
        unknownKeysStrategy,
        keys = other.keys().unsafeCast(),
        readSafety = readSafety
    )

    private val serializer = SchemaSerializer<DynoMap<DynoKey<*>>>(this, unknownKeysStrategy) { data, json ->
        MutableDynoMap(data, json, readSafety)
    }

    override fun getSerializer(): KSerializer<DynoMap<DynoKey<*>>> = serializer

    final override fun name(): String = name

    final override fun version(): Int = version
}


