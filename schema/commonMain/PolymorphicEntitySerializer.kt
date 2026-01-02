package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

open class PolymorphicEntitySerializer<T: Entity<*>>: PolymorphicDynoSerializer<T> {
    private var threadSafeRead: Boolean = true

    constructor(
        baseSchemaName: String,
        baseSchemaVersion: Int = -1,
        discriminator: String = DEFAULT_DISCRIMINATOR,
        unknownKeysStrategy: UnknownKeysStrategy = DEFAULT_UNKNOWN_KEY_STRATEGY,
        threadSafeRead: Boolean = true
    ): super(
        baseSchemaName = baseSchemaName,
        baseSchemaVersion = baseSchemaVersion,
        unknownKeysStrategy,
        discriminator = discriminator
    ) {
        this.threadSafeRead = threadSafeRead
    }

    constructor(baseKeySerializer: KSerializer<*>): super(baseKeySerializer)

    override fun getMap(schema: DynoSchema, data: MutableMap<Any, Any>?, json: Json?): T =
        MutableEntity(schema, data, json, threadSafeRead = threadSafeRead).unsafeCast()
}