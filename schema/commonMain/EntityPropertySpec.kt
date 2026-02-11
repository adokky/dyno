package dev.dokky.dyno

import kotlinx.serialization.KSerializer
import kotlin.reflect.KProperty
import kotlin.reflect.KType

abstract class EntityPropertySpec<T, SBase: DynoSchema>(
    protected val serializer: KSerializer<T & Any>,
    protected val type: KType,
    protected val name: String?,
    protected val onAssign: DynoKeyProcessor<T & Any>?,
    protected val onDecode: DynoKeyProcessor<T & Any>?
): DynoKeySpec<T & Any> {
    final override val DynoKeySpec.Internal.onAssign: DynoKeyProcessor<T & Any>?
        get() = this@EntityPropertySpec.onAssign
    final override val DynoKeySpec.Internal.onDecode: DynoKeyProcessor<T & Any>?
        get() = this@EntityPropertySpec.onDecode

    protected inline fun <S: DynoSchema, R: EntityProperty<S, T>> S.createProperty(
        property: KProperty<*>,
        createProperty: (name: String, index: Int) -> R
    ): R {
        return createProperty(name ?: property.name, keyCount())
            .also { prop -> register(prop) }
    }

    protected fun DynoSchema.register(prop: DynoKey<*>) {
        if (this is AbstractDynoSchema<*>) register(prop)
    }

    abstract operator fun <S: SBase> provideDelegate(
        thisRef: S,
        property: KProperty<*>
    ): EntityProperty<S, T>
}