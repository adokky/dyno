package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

open class PolymorphicEntitySerializer<T: Entity<*>>: PolymorphicDynoSerializer<T> {
    constructor(
        baseSchemaName: String,
        discriminator: String = DEFAULT_DISCRIMINATOR,
        unknownKeysStrategy: UnknownKeysStrategy = UnknownKeysStrategy.KeepIfJsonAllowed
    ): super(
        baseSchemaName = baseSchemaName,
        unknownKeysStrategy,
        discriminator = discriminator
    )

    constructor(baseKeySerializer: KSerializer<*>): super(baseKeySerializer)

    override fun getMap(schema: DynoSchema, data: MutableMap<Any, Any>?, json: Json?): T =
        MutableEntity(schema, data, json).unsafeCast()
}