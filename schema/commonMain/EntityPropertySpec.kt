package dev.dokky.dyno

import kotlinx.serialization.KSerializer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType

abstract class EntityPropertySpec<T> @PublishedApi internal constructor(
    protected val serializer: KSerializer<T & Any>,
    protected val type: KType,
    protected val name: String?,
    protected val onAssign: DynoKeyProcessor<T & Any>?,
    protected val onDecode: DynoKeyProcessor<T & Any>?
): DynoKeySpec<T & Any> {
    protected constructor(
        serializer: KSerializer<T & Any>,
        type: KType,
        name: String? = null,
    ): this(serializer, type, name, onAssign = null, onDecode = null)

    final override val DynoKeySpec.Internal.onAssign: DynoKeyProcessor<T & Any>?
        get() = this@EntityPropertySpec.onAssign
    final override val DynoKeySpec.Internal.onDecode: DynoKeyProcessor<T & Any>?
        get() = this@EntityPropertySpec.onDecode

    protected abstract fun <S: DynoSchema> createProperty(name: String, index: Int): EntityProperty<S, T>

    @UnsafeDynoApi
    operator fun <S: DynoSchema> provideDelegate(
        thisRef: S,
        property: KProperty<*>
    ): ReadOnlyProperty<Any, EntityProperty<S, T>> {
        return createProperty<S>(
            name = name ?: property.name,
            index = thisRef.keyCount()
        ).also {
            if (thisRef is AbstractDynoSchema<*>) thisRef.register(it)
        }
    }
}