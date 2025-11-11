package dyno

import kotlinx.serialization.KSerializer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class DynoKeyPrototype<T> @PublishedApi internal constructor(
    private val serializer: KSerializer<T & Any>,
    private val propertyName: String? = null,
    private val onAssign: DynoKeyProcessor<T & Any>? = null,
    private val onDecode: DynoKeyProcessor<T & Any>? = null
): DynoKeySpec<T & Any> {
    override val DynoKeySpec.Internal.onAssign: DynoKeyProcessor<T & Any>?
        get() = this@DynoKeyPrototype.onAssign

    override val DynoKeySpec.Internal.onDecode: DynoKeyProcessor<T & Any>?
        get() = this@DynoKeyPrototype.onDecode

    override fun DynoKeySpec.Internal.copy(
        onAssign: DynoKeyProcessor<T & Any>?,
        onDecode: DynoKeyProcessor<T & Any>?
    ): DynoKeyPrototype<T> =
        DynoKeyPrototype(serializer, onAssign = onAssign, onDecode = onDecode)

    @UnsafeDynoApi
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any, DynoKey<T>> =
        SimpleDynoKey(propertyName ?: property.name, serializer, onAssign, onDecode)
}