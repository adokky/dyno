package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

// todo default schema
@ExperimentalDynoApi
@OptIn(ExperimentalSerializationApi::class)
abstract class AbstractPolymorphicDynoSerializer<T: DynoMapBase>(
    private val discriminator: String,
    private val serialName: String,
    /**
     * Controls whether unknown keys should be kept in the deserialized map [T] when
     * [kotlinx.serialization.json.JsonConfiguration.ignoreUnknownKeys] is enabled.
     *
     * If `true`, unknown keys will be stored in the resulting map [T] as [JsonElement] values.
     * If `false`, unknown keys will be ignored and not included in the result.
     * If `null`, [kotlinx.serialization.json.JsonConfiguration.ignoreUnknownKeys] will be used as default.
     *
     * This property is useful when the [resolve] implementation doesn't know all the required keys,
     * because some of them are defined dynamically outside.
     */
    protected val keepUnknownKeys: Boolean? = null // todo enum strategy
): AbstractEagerDynoSerializer<T>() {
    /**
     * Constructs [AbstractPolymorphicDynoSerializer] instance using "base" key [KSerializer].
     * "Base" key is type argument of [DynoMapBase] (upper bound).
     *
     * It is only required [baseKeySerializer] to implement [KSerializer.descriptor],
     * the actual serialization methods are not used.
     *
     * @see DynoMapBase
     */
    constructor(baseKeySerializer: KSerializer<*>): this(
        discriminator = "type",
        serialName = baseKeySerializer.descriptor.serialName
    )

    final override fun resolve(context: ResolveContext): ResolveResult {
        if (context.keyString == discriminator) {
            val schema = context.decodeValue<String>()
                ?: throw SerializationException("'$discriminator' can not be null") // todo use default schema
            context.state = resolveSchema(context.json, schema)
                ?: throw SerializationException("no such schema '$schema'")
            return ResolveResult.Skip
        }

        return ResolveResult.Delay
    }

    final override fun postResolve(context: ResolveContext): ResolveResult {
        val schema = (context.state as? DynoKeyProvider) ?: throwMissingSchema()

        val dynoKey = schema.getDynoKey(context.json.serializersModule, context.keyString)
        if (dynoKey != null) return dynoKey

        val ignore = keepUnknownKeys ?: context.json.configuration.ignoreUnknownKeys
        if (!ignore) throw NoSuchDynoKeySerializationException(context.keyString, schema.name)
        return ResolveResult.Keep
    }

    private fun resolveSchema(json: Json, schema: String): DynoKeyProvider? {
        val ctxElement = json.serializersModule
            .getContextual(DynamicObjectKeyRegistry.ContextElement::class) as DynamicObjectKeyRegistry.ContextElement?
            ?: error(
                "No dyno key providers are registered in the SerializersModule. " +
                "They must be provided with `SerializersModule { dynamicObjectKeys(/**/) }`"
            )
        return ctxElement.registry.schemaToProvider(schema) ?: ctxElement.registry.default
    }

    private fun throwMissingSchema(): Nothing =
        throw MissingFieldException(missingField = discriminator, serialName = serialName)

    final override fun createMap(state: Any?, data: HashMap<Any, Any>?, json: Json?): T {
        if (state == null) throwMissingSchema()
        return getMap(data, json)
    }

    protected abstract fun getMap(data: HashMap<Any, Any>?, json: Json?): T
}

internal sealed class AbstractPolymorphicDynoSerializerBase<T : DynoMapBase>: AbstractPolymorphicDynoSerializer<T> {
    constructor(serialName: String, discriminator: String, keepUnknownKeys: Boolean?): super(
        serialName = serialName,
        discriminator = discriminator,
        keepUnknownKeys = keepUnknownKeys
    )

    constructor(baseKeySerializer: KSerializer<*>): super(baseKeySerializer)

    override fun getMap(data: HashMap<Any, Any>?, json: Json?): T =
        DynamicObjectImpl(data, json).unsafeCast()
}