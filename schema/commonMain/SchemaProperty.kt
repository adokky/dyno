package dyno

import kotlinx.serialization.KSerializer
import kotlin.jvm.JvmRecord
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A basic implementation of [DynoKey].
 * Similar to [SimpleDynoKey] but bound to specific [DynoSchema] (sub)type [S].
 */
@ConsistentCopyVisibility
@JvmRecord // ensures constant folding for static SimpleDynoKey fields
data class SchemaProperty<in S: DynoSchema, T> internal constructor(
    override val name: String,
    override val serializer: KSerializer<T & Any>,
    internal val index: Int,
    override val onAssign: DynoKeyProcessor<T & Any>? = null,
    override val onDecode: DynoKeyProcessor<T & Any>? = null,
) : DynoKey<T>,
    Comparable<DynoKey<T>>,
    ReadOnlyProperty<Any, SchemaProperty<S, T>>,
    DynoKeySpec<T & Any>
{
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean =
        this === other || (other as? DynoKey<*>)?.name == name

    override fun hashCode(): Int = name.hashCode()

    override fun compareTo(other: DynoKey<T>): Int = name.compareTo(other.name)

    override fun getValue(thisRef: Any, property: KProperty<*>): SchemaProperty<S, T> = this

    override val DynoKeySpec.Internal.onAssign: DynoKeyProcessor<T & Any>?
        get() = this@SchemaProperty.onAssign
    override val DynoKeySpec.Internal.onDecode: DynoKeyProcessor<T & Any>?
        get() = this@SchemaProperty.onDecode

    override fun DynoKeySpec.Internal.copy(
        onAssign: DynoKeyProcessor<T & Any>?,
        onDecode: DynoKeyProcessor<T & Any>?
    ): SchemaProperty<S, T & Any> =
        SchemaProperty(name, serializer, index, onAssign = onAssign, onDecode = onAssign)
}