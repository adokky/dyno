package dyno

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmField

@ExperimentalDynoApi
@OptIn(ExperimentalSerializationApi::class)
abstract class PolymorphicDynoSerializer<T: Entity<*>>(
    private val baseSchemaName: String,
    private val baseSchemaVersion: Int = -1,
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
    protected val unknownKeysStrategy: UnknownKeysStrategy = DEFAULT_UNKNOWN_KEY_STRATEGY,
    private val registry: PolymorhicSchemaRegistry? = null,
    private val discriminator: String = DEFAULT_DISCRIMINATOR
): AbstractEagerDynoSerializer<T>() {
    /**
     * Constructs [PolymorphicDynoSerializer] instance using "base" key [KSerializer].
     * "Base" key is type argument of [Entity] (upper bound).
     *
     * It is only required [baseKeySerializer] to implement [KSerializer.descriptor],
     * the actual serialization methods are not used.
     *
     * @see Entity
     */
    constructor(baseKeySerializer: KSerializer<*>): this(
        baseSchemaName = baseKeySerializer.descriptor.serialName,
        discriminator = DEFAULT_DISCRIMINATOR
    )

    private class State(val baseSchema: SimpleSchemaRegistry) {
        var schema: DynoSchema? = null
        var checkerState: Any? = null
    }

    final override fun initContextState(json: Json): Any = State(
        when (val registry = getRegistry(json)) {
            is PolymorhicSchemaRegistry -> registry.get(baseSchemaName, baseSchemaVersion)
                ?: throwSchemaNotFound(baseSchemaName)
            is SimpleSchemaRegistry -> registry
        }
    )

    final override fun resolve(context: ResolveContext): ResolveResult {
        val state = context.state as State
        state.schema?.let { return resolveKey(context, state, it) }

        if (context.keyString == discriminator) {
            val schemaName = context.decodeValue<String>()
            state.resolveSchemaOrThrow(schemaName) {
                throwSchemaNotFound(schemaName)
            }
            return ResolveResult.Skip
        }

        return ResolveResult.Delay
    }

    final override fun postResolve(context: ResolveContext): ResolveResult {
        val state = (context.state as State)
        val schema = state.schema ?: state.resolveSchemaOrThrow(name = null, ::throwMissingSchema)
        return resolveKey(context, state, schema)
    }

    private inline fun State.resolveSchemaOrThrow(name: String?, orElse: () -> Nothing): DynoSchema {
        val resolved = if (name == null) baseSchema.fallback else baseSchema.get(name)
        resolved ?: orElse()
        schema = resolved
        return resolved
    }

    private fun resolveKey(context: ResolveContext, state: State, schema: DynoSchema): ResolveResult {
        val key = schema.getKey(context.json.serializersModule, context.keyString)
        if (key != null) {
            state.checkerState = schema.checker().markIsPresent(state.checkerState, key)
            return key
        }

        if (context.keyString == discriminator) {
            throw SerializationException("discriminator appear twice")
        }

        return unknownKeysStrategy.process(context, schema)
    }

    private fun getRegistry(json: Json): DynoSchemaRegistry =
        registry ?: contextElement(json).registry

    private fun contextElement(json: Json): ContextElement =
        json.serializersModule
            .getContextual(ContextElement::class) as ContextElement?
            ?: error(
                "No dyno schema registry is registered in the SerializersModule. " +
                "It must be provided with `SerializersModuleBuilder.dynoSchemaRegistry` extension"
            )

    override fun CompositeEncoder.encodeCustomKeys(value: T): Int {
        if (discriminator in value) return 0
        encodeStringElement(descriptor, 0, discriminator)
        encodeStringElement(descriptor, 1, value.schema.name())
        return 2
    }

    private fun throwSchemaNotFound(name: String?): Nothing {
        if (name == null) throwMissingSchema() else throw SerializationException("Schema not found: '$name'")
    }

    private fun throwMissingSchema(): Nothing =
        throw MissingFieldException(missingField = discriminator, serialName = baseSchemaName)

    final override fun createMap(state: Any?, data: MutableMap<Any, Any>?, json: Json?): T {
        val state = state as State
        val schema = state.schema ?: throwMissingSchema()
        var unwrapped = schema
        val checker: EntityConsistencyChecker
        when(schema) {
            is AbstractDynoSchema<*> -> { checker = schema.checker }
            is DynoSchemaWithChecker -> {
                checker = schema.checker
                unwrapped = schema.original
            }
            else -> throwNoConsistencyChecker(schema)
        }
        checker.check(state.checkerState)
        return getMap(unwrapped, data, json)
    }

    protected abstract fun getMap(schema: DynoSchema, data: MutableMap<Any, Any>?, json: Json?): T

    companion object {
        private val emptyHashMap = HashMap<Any, Any>(0)

        const val DEFAULT_DISCRIMINATOR = "type"

        @JvmField
        val DEFAULT_UNKNOWN_KEY_STRATEGY: UnknownKeysStrategy = UnknownKeysStrategy.KeepIfJsonAllowed
    }
}