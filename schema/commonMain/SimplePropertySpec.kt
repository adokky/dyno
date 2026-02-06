package dev.dokky.dyno

import kotlinx.serialization.KSerializer
import kotlin.reflect.KType

class SimplePropertySpec<T> @PublishedApi internal constructor(
    serializer: KSerializer<T & Any>,
    type: KType,
    name: String? = null,
    onAssign: DynoKeyProcessor<T & Any>? = null,
    onDecode: DynoKeyProcessor<T & Any>? = null
): EntityPropertySpec<T>(
    serializer, type,
    name = name,
    onAssign = onAssign,
    onDecode = onDecode
) {
    override fun DynoKeySpec.Internal.copy(
        onAssign: DynoKeyProcessor<T & Any>?,
        onDecode: DynoKeyProcessor<T & Any>?
    ): SimplePropertySpec<T> =
        SimplePropertySpec(serializer, type, name = name, onAssign = onAssign, onDecode = onDecode)

    override fun <S: DynoSchema> createProperty(name: String, index: Int): SimpleProperty<S, T> {
        return SimpleProperty(
            name = name,
            serializer = serializer,
            type = type,
            index = index,
            onAssign = onAssign,
            onDecode = onDecode
        )
    }
}