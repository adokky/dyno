package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

@PublishedApi
internal fun classMapStringKey(klass: KClass<*>): String {
    @OptIn(ExperimentalSerializationApi::class)
    return serializer(klass, emptyList(), isNullable = false).descriptor.serialName
}

@PublishedApi
internal fun classMapStringKey(ktype: KType): String {
    return serializer(ktype).descriptor.serialName
}

@PublishedApi
internal inline fun <reified T: Any> dynoKey(): DynoKey<T> {
    val serializer = serializer<T>()
    return DynoKey(serializer.descriptor.serialName, serializer)
}

@PublishedApi
internal fun <T : Any> dynoKey(klass: KClass<T>): DynoKey<T> {
    @OptIn(ExperimentalSerializationApi::class)
    val serializer = serializer(klass, emptyList(), isNullable = false)
    return DynoKey(serializer.descriptor.serialName, serializer.unsafeCast())
}

@PublishedApi
internal fun <T : Any> dynoKey(type: KType): DynoKey<T> {
    val serializer = serializer(type)
    return DynoKey(serializer.descriptor.serialName, serializer.unsafeCast())
}