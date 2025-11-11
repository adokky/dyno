package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer

open class SimpleDynoSchema internal constructor(
    private val name: String,
    unknownKeysStrategy: UnknownKeysStrategy,
    keys: Collection<DynoKey<*>>
): AbstractDynoSchema<DynoMap<DynoKey<*>>>(keys = keys) {
    constructor(
        name: String,
        unknownKeysStrategy: UnknownKeysStrategy = UnknownKeysStrategy.KeepIfJsonAllowed
    ): this(name = name, unknownKeysStrategy, keys = emptyList())

    constructor(
        other: DynoSchema,
        name: String? = null,
        unknownKeysStrategy: UnknownKeysStrategy? = null
    ): this(
        name = name ?: other.name(),
        unknownKeysStrategy ?: UnknownKeysStrategy.KeepIfJsonAllowed,
        keys = other.keys().unsafeCast()
    )

    private val serializer = SchemaSerializer<DynoMap<DynoKey<*>>>(this, unknownKeysStrategy) { data, json ->
        MutableDynoMap(data, json)
    }

    override fun serializer(): KSerializer<DynoMap<DynoKey<*>>> = serializer

    override fun name(): String = name
}


