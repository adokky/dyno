package dyno

import kotlinx.serialization.KSerializer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class SchemaPropertySpec<T> @PublishedApi internal constructor(
    private val serializer: KSerializer<T & Any>,
    private val name: String? = null,
    private val onAssign: DynoKeyProcessor<T & Any>? = null,
    private val onDecode: DynoKeyProcessor<T & Any>? = null
): DynoKeySpec<T & Any> {
    override val DynoKeySpec.Internal.onAssign: DynoKeyProcessor<T & Any>?
        get() = this@SchemaPropertySpec.onAssign
    override val DynoKeySpec.Internal.onDecode: DynoKeyProcessor<T & Any>?
        get() = this@SchemaPropertySpec.onDecode

    override fun DynoKeySpec.Internal.copy(
        onAssign: DynoKeyProcessor<T & Any>?,
        onDecode: DynoKeyProcessor<T & Any>?
    ): SchemaPropertySpec<T> =
        SchemaPropertySpec(serializer, name, onAssign = onAssign, onDecode = onDecode)

    @UnsafeDynoApi
    operator fun <S: DynoSchema> provideDelegate(
        thisRef: S,
        property: KProperty<*>
    ): ReadOnlyProperty<Any, SchemaProperty<S, T>> {
        return SchemaProperty<S, T>(
            name = name ?: property.name,
            serializer = serializer,
            index = thisRef.keyCount(),
            onAssign = onAssign,
            onDecode = onDecode
        ).also {
            if (thisRef is AbstractDynoSchema<*>) thisRef.register(it)
        }
    }
}