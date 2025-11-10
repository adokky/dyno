package dyno

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName
import kotlin.jvm.JvmRecord
import kotlin.jvm.JvmStatic
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A basic implementation of [DynoKey].
 *
 * Keys are compared by their [name] only. This means that two keys with the same name
 * but different types will be considered equal:
 * ```
 * val key1 = SimpleDynoKey("count", Int.serializer())
 * val key2 = SimpleDynoKey("count", Long.serializer())
 * // key1 == key2 is true!
 * ```
 * It is the user's responsibility to ensure key names are unique across types if needed.
 */
@JvmRecord // ensures constant folding for static SimpleDynoKey fields
data class SimpleDynoKey<T>(
    override val name: String,
    override val serializer: KSerializer<T & Any>,
    override val onAssign: DynoKeyProcessor<T & Any>? = null,
    override val onDecode: DynoKeyProcessor<T & Any>? = null
) : DynoKey<T>,
    Comparable<DynoKey<T>>,
    ReadOnlyProperty<Any, DynoKey<T>>,
    DynoKeySpec<T>
{
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean =
        this === other || (other as? DynoKey<*>)?.name == name

    override fun hashCode(): Int = name.hashCode()

    override fun compareTo(other: DynoKey<T>): Int = name.compareTo(other.name)

    override fun getValue(thisRef: Any, property: KProperty<*>): DynoKey<T> = this

    companion object {
        /**
         * Creates a new [SimpleDynoKey] with [serializer] for the type [T] the given [name].
         */
        @JvmName("get")
        @JvmStatic
        inline operator fun <reified T: Any> invoke(name: String): SimpleDynoKey<T> {
            return SimpleDynoKey(name, serializer<T>())
        }
    }
}