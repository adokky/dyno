package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

sealed class AbstractDynoSchema<M: DynoMap<*>>(
    keys: Collection<DynoKey<*>> = emptyList(),
): DynoSchema, KSerializer<M> {
    private val keys = HashMap<String, SchemaProperty<*, *>>()

    @PublishedApi
    internal val checker by lazy { EntityConsistencyChecker(this) }

    init {
        keys.forEach(::register)
    }

    protected abstract fun getSerializer(): KSerializer<M>

    @Deprecated(
        "Protected from direct usage to avoid interference with declared key properties. " +
        "Cast the type to KSerializer<*> to get access",
        level = DeprecationLevel.HIDDEN
    )
    final override val descriptor: SerialDescriptor
        get() = getSerializer().descriptor

    final override fun serialize(encoder: Encoder, value: M): Unit =
        getSerializer().serialize(encoder, value)

    final override fun deserialize(decoder: Decoder): M =
        getSerializer().deserialize(decoder).unsafeCast()

    override fun getKey(serializersModule: SerializersModule, name: String): DynoKey<*>? = keys[name]

    internal fun register(key: DynoKey<*>) {
        check(tryRegister(key)) {
            "attempt to declare key '${key.name}' twice"
        }
    }

    internal fun tryRegister(key: DynoKey<*>): Boolean {
        val key = if (key is SchemaProperty<*, *> && key.index == keys.size) key else {
            @Suppress("UNCHECKED_CAST")
            key as DynoKey<Any>
            SchemaProperty<DynoSchema, Any>(key.name, key.serializer, keys.size, key.onAssign, key.onDecode)
        }
        return keys.put(key.name, key) == null
    }

    override fun keyCount(): Int = keys.size

    override fun keys(): Collection<DynoKey<*>> = keys.values


    /**
     * Creates a [SchemaPropertySpec] for type [T] with an optional [name].
     *
     * If [name] is not provided, it will be inferred from the property name
     * when used with `by` delegate syntax.
     *
     * The resulting prototype can be further configured with [onAssign], [onDecode],
     * or [validate] methods.
     *
     * Example:
     * ```
     * object Person: SimpleDynoSchema {
     * val name by dynoKey<String>() // name inferred from property
     * val age by dynoKey<Int>("userAge") // explicit name
     *
     * val email by dynoKey<String>()
     *     .validate { require("@" in it) { "Invalid email" } }
     * }
     * ```
     */
    inline fun <reified T> dynoKey(
        name: String? = null,
        serializer: KSerializer<T & Any> = kotlinx.serialization.serializer<T>().unsafeCast()
    ): SchemaPropertySpec<T> =
        SchemaPropertySpec(serializer = serializer, name = name)

    fun <S2: AbstractDynoSchema<M>, M: DynoMap<SchemaProperty<S2, *>>>
            dynoKey(schema: S2): SchemaPropertySpec<M> =
        SchemaPropertySpec(serializer = schema.unsafeCast())

    fun <S2: EntitySchema> dynoKey(schema: S2): SchemaPropertySpec<Entity<S2>> =
        SchemaPropertySpec(serializer = schema.unsafeCast())
}