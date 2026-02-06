package dev.dokky.dyno

import kotlinx.serialization.KSerializer
import kotlin.jvm.JvmRecord
import kotlin.reflect.KProperty
import kotlin.reflect.KType

/**
 * A basic implementation of [EntityProperty].
 *
 * Similar to [SimpleDynoKey] but bound to specific [DynoSchema] (sub)type [S].
 */
@ConsistentCopyVisibility
@JvmRecord // ensures constant folding for static fields
data class SimpleProperty<in S: DynoSchema, T> internal constructor(
    override val name: String,
    override val serializer: KSerializer<T & Any>,
    override val type: KType,
    private val index: Int,
    override val onAssign: DynoKeyProcessor<T & Any>? = null,
    override val onDecode: DynoKeyProcessor<T & Any>? = null,
) : EntityProperty<S, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): SimpleProperty<S, T> = this

    override fun DynoKeySpec.Internal.copy(
        onAssign: DynoKeyProcessor<T & Any>?,
        onDecode: DynoKeyProcessor<T & Any>?
    ): SimpleProperty<S, T & Any> =
        SimpleProperty(name, serializer, type, index, onAssign = onAssign, onDecode = onAssign)

    override val DynoKeySpec.Internal.index: Int get() = this@SimpleProperty.index

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean =
        this === other || (other as? DynoKey<*>)?.name == name

    override fun hashCode(): Int = name.hashCode()

    override fun compareTo(other: DynoKey<T>): Int = name.compareTo(other.name)

    override val DynoKeySpec.Internal.onAssign: DynoKeyProcessor<T & Any>?
        get() = this@SimpleProperty.onAssign

    override val DynoKeySpec.Internal.onDecode: DynoKeyProcessor<T & Any>?
        get() = this@SimpleProperty.onDecode
}