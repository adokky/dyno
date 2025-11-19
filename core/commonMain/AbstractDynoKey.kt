package dyno

import kotlinx.serialization.KSerializer
import kotlin.reflect.KType

abstract class AbstractDynoKey<T>(
    override val name: String,
    override val serializer: KSerializer<T & Any>,
    override val type: KType,
    override val onAssign: DynoKeyProcessor<T & Any>? = null,
    override val onDecode: DynoKeyProcessor<T & Any>? = null
) : DynoKey<T>, Comparable<DynoKey<T>> {
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean =
        this === other || (other as? DynoKey<*>)?.name == name

    override fun hashCode(): Int = name.hashCode()

    override fun compareTo(other: DynoKey<T>): Int = name.compareTo(other.name)
}