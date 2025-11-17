package dyno

import kotlinx.serialization.json.Json

internal abstract class SchemaSerializer<M: DynoMap<*>>(
    schema: DynoSchema,
    val unknownKeysStrategy: UnknownKeysStrategy
): AbstractEagerDynoSerializer<M>() {
    private val schema: DynoSchema = schema.withChecker()

    private fun Int.isPropertyPresent(index: Int): Boolean = (this and (1 shl index)) != 0

    override fun resolve(context: ResolveContext): ResolveResult {
        val resolved = schema.getKey(context.json.serializersModule, context.keyString)
        if (resolved != null) {
            context.state = schema.checker().markIsPresent(context.state, resolved)
        }
        return resolved ?: unknownKeysStrategy.process(context, schema)
    }

    abstract fun getMap(data: MutableMap<Any, Any>?, json: Json?): M

    final override fun createMap(state: Any?, data: MutableMap<Any, Any>?, json: Json?): M {
        schema.checker().check(state)
        return getMap(data, json)
    }
}

internal fun <M: DynoMap<*>> SchemaSerializer(
    schema: DynoSchema,
    unknownKeysStrategy: UnknownKeysStrategy,
    getMap: (data: MutableMap<Any, Any>?, json: Json?) -> M
): SchemaSerializer<M> = object : SchemaSerializer<M>(schema, unknownKeysStrategy) {
    override fun getMap(
        data: MutableMap<Any, Any>?,
        json: Json?
    ): M = getMap(data, json)
}