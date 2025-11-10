package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty

class DynoKeyPrototype<T> @PublishedApi internal constructor(
    internal val serializer: KSerializer<T & Any>,
    internal val onAssign: DynoKeyProcessor<T & Any>? = null,
    internal val onDecode: DynoKeyProcessor<T & Any>? = null
): DynoKeySpec<T> {
    internal constructor(
        other: DynoKeyPrototype<T>,
        serializer: KSerializer<T & Any> = other.serializer,
        onAssign: DynoKeyProcessor<T & Any>? = other.onAssign,
        onDecode: DynoKeyProcessor<T & Any>? = other.onDecode
    ): this(serializer, onAssign, onDecode)

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): SimpleDynoKey<T> {
        val key = SimpleDynoKey<T>(property.name, serializer, onAssign, onDecode)
        if (thisRef is DynoSchema) thisRef.register(key)
        return key
    }
}

inline fun <reified T: Any> dynoKey(): DynoKeyPrototype<T> =
    DynoKeyPrototype(serializer<T>())

inline fun <reified T: Any> dynoOptionalKey(): DynoKeyPrototype<T?> =
    dynoKey<T>().optional()

fun <T: Any> DynoKeyPrototype<T>.optional(): DynoKeyPrototype<T?> = unsafeCast()

object TestDK: DynoSchema() {
    val k2 by dynoKey<Int>().optional()
    val k3 by dynoKey<Int>().validate { println(name + it) }

    fun test() {

    }
}