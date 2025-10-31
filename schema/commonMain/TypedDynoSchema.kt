package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

private typealias TypedKeyDelegate<R, T> = PropertyDelegateProvider<R, ReadOnlyProperty<R, SchemaProperty<R, T>>>

// todo move 'key' outside, to be able extend schemas from outside
abstract class TypedDynoSchema<S: TypedDynoSchema<S>>(
    internal val schemaName: String
): DynoKeyProvider, KSerializer<Entity<S>> {
    private val keys = HashMap<String, DynoKey<*>>()

    internal fun register(key: SchemaProperty<*, *>) {
        check(keys.put(key.name, key) == null) {
            "attempt to define key '${key.name}' twice"
        }
    }

    override fun getDynoKey(serializersModule: SerializersModule, name: String): SchemaProperty<*, *>? =
        keys[name].unsafeCast()

    private val serializer = object : AbstractEagerDynoSerializer<Entity<S>>() {
        override fun resolve(context: ResolveContext): ResolveResult {
            return getDynoKey(context.json.serializersModule, context.keyString)
                ?: ResolveResult.Keep
        }

        override fun createMap(
            state: Any?,
            data: MutableMap<Any, Any>?,
            json: Json?
        ): Entity<S> {
            return EntityImpl(data, json)
        }
    }

    final override val descriptor: SerialDescriptor get() =
        serializer.descriptor

    final override fun serialize(encoder: Encoder, value: Entity<S>): Unit =
        serializer.serialize(encoder, value)

    final override fun deserialize(decoder: Decoder): Entity<S> =
        serializer.deserialize(decoder).unsafeCast()

    protected fun <T : Any> key(name: String, serializer: KSerializer<T>): SchemaProperty<S, T> =
        SchemaProperty<S, T>(name, serializer).also(::register)

    protected inline fun <reified T: Any> key(name: String): SchemaProperty<S, T> =
        key(name, serializer<T>())

    protected inline fun <reified T: Any> S.key(): TypedKeyDelegate<S, T> = key(serializer<T>())

    private fun <T: Any> S.delegate(serializer: KSerializer<T>): TypedKeyDelegate<S, T> =
        TypedKeyDelegate { _, property ->
            SchemaProperty<S, T>(property.name, serializer)
                .also(::register)
        }

    protected fun <T: Any> S.key(serializer: KSerializer<T>): TypedKeyDelegate<S, T> =
        delegate(serializer)
}