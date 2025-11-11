package dyno

import kotlinx.serialization.KSerializer

abstract class DynoMapSchema(
    name: String,
    unknownKeysStrategy: UnknownKeysStrategy = UnknownKeysStrategy.KeepIfJsonAllowed
): AbstractDynoSchema<DynoMap<SchemaProperty<*, *>>>() {

    private val serializer = SchemaSerializer<DynoMap<SchemaProperty<*, *>>>(this, unknownKeysStrategy) { data, json ->
        MutableDynoMap(data, json)
    }

    override fun serializer(): KSerializer<DynoMap<SchemaProperty<*, *>>> = serializer
}