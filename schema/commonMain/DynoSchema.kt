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

private typealias KeyDelegate<R, T> = PropertyDelegateProvider<R, ReadOnlyProperty<R, DynoKey<T>>>

abstract class DynoSchema(
    internal val schemaName: String
): DynoKeyProvider, KSerializer<Entity<*>> {
    private val keys = HashMap<String, DynoKey<*>>()

    internal fun register(key: DynoKey<*>) {
        check(keys.put(key.name, key) == null) {
            "attempt to define key '${key.name}' twice"
        }
    }

    override fun getDynoKey(serializersModule: SerializersModule, name: String): DynoKey<*>? = keys[name]

    private val serializer = object : AbstractEagerDynoSerializer<Entity<*>>() {
        override fun resolve(context: ResolveContext): ResolveResult {
            return getDynoKey(context.json.serializersModule, context.keyString)
                ?: ResolveResult.Keep
        }

        override fun createMap(
            state: Any?,
            data: MutableMap<Any, Any>?,
            json: Json?
        ): Entity<*> {
            return EntityImpl(data, json)
        }
    }

    final override val descriptor: SerialDescriptor
        get() =
        serializer.descriptor

    final override fun serialize(encoder: Encoder, value: Entity<*>): Unit =
        serializer.serialize(encoder, value)

    final override fun deserialize(decoder: Decoder): Entity<*> =
        serializer.deserialize(decoder).unsafeCast()

    protected fun <T : Any> key(name: String, serializer: KSerializer<T>): DynoKey<T> =
        DynoKey<T>(name, serializer).also(::register)

    protected inline fun <reified T: Any> key(name: String): DynoKey<T> =
        key(name, serializer<T>())

    protected inline fun <reified T: Any> key(): KeyDelegate<DynoSchema, T> = key(serializer<T>())

    private fun <T: Any> delegate(serializer: KSerializer<T>): KeyDelegate<DynoSchema, T> =
        TypedKeyDelegate { _, property ->
            SchemaProperty<DynoSchema, T>(property.name, serializer)
                .also(::register)
        }

    protected fun <T: Any> key(serializer: KSerializer<T>): KeyDelegate<DynoSchema, T> =
        delegate(serializer)

    // DynoSchema should not have any properties or property-like declarations,
    // as those could conflict with user-defined ones.
    // These properties are "hidden" by using additional receiver.

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    @InternalDynoApi
    object Meta

    @Suppress("UnusedReceiverParameter", "Deprecation_Error")
    val Meta.ketCount: Int get() = keys.size

    @Suppress("UnusedReceiverParameter", "Deprecation_Error")
    val Meta.schemaName: String get() = this@DynoSchema.schemaName

    @Suppress("Deprecation_Error")
    @ExperimentalDynoApi
    fun <R> withMeta(body: Meta.() -> R): R = Meta.body()
}