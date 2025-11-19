package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@PublishedApi
internal fun classMapStringKey(klass: KClass<*>): String {
    @OptIn(ExperimentalSerializationApi::class)
    return serializer(klass, emptyList(), isNullable = false).descriptor.serialName
}

@PublishedApi
internal fun classMapStringKey(type: KType): String {
    return serializer(type).descriptor.serialName
}

@PublishedApi
internal fun <T : Any> dynoKey(klass: KClass<T>): DynoKey<T> {
    @OptIn(ExperimentalSerializationApi::class)
    val serializer = serializer(klass, emptyList(), isNullable = false)
    return DynoKey(serializer.descriptor.serialName, serializer.unsafeCast(), typeOf<Unit>())
}

@PublishedApi
internal fun <T : Any> dynoKey(type: KType): DynoKey<T> {
    val serializer = serializer(type)
    return DynoKey(serializer.descriptor.serialName, serializer.unsafeCast(), type)
}