package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class DynoKeyPrototype<T> @PublishedApi internal constructor(
    internal val serializer: KSerializer<T & Any>,
    internal val onAssign: DynoKeyProcessor<T & Any>? = null,
    internal val onDecode: DynoKeyProcessor<T & Any>? = null
): DynoKeySpec<T & Any> {
    internal constructor(
        other: DynoKeyPrototype<T>,
        serializer: KSerializer<T & Any> = other.serializer,
        onAssign: DynoKeyProcessor<T & Any>? = other.onAssign,
        onDecode: DynoKeyProcessor<T & Any>? = other.onDecode
    ): this(serializer, onAssign, onDecode)

    @InternalDynoApi
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any, DynoKey<T>> {
        val key = SimpleDynoKey<T>(property.name, serializer, onAssign, onDecode)
        if (thisRef is DynoSchema) with(thisRef) {
            DynoSchema.Internal.register(key)
        }
        return key
    }
}

inline fun <reified T> dynoKey(): DynoKeyPrototype<T> =
    DynoKeyPrototype(serializer<T>().unsafeCast<KSerializer<T & Any>>())

fun <T: Any> DynoKeyPrototype<T>.optional(): DynoKeyPrototype<T?> = unsafeCast()