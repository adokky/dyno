package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.json.Json

internal sealed class AbstractTypedClassMapSerializer<T: TypedClassMap<*>>: AbstractDynoSerializer<T>() {
    override fun createMap(data: MutableMap<Any, Any>?, json: Json?): T =
        MutableTypedClassMap<Any>(data, json).unsafeCast()
}

internal sealed class AbstractClassMapSerializer<T: ClassMap>: AbstractDynoSerializer<T>() {
    override fun createMap(data: MutableMap<Any, Any>?, json: Json?): T =
        MutableClassMap(data, json).unsafeCast()
}

internal object ClassMapSerializer: AbstractClassMapSerializer<ClassMap>()
internal object MutableClassMapSerializer: AbstractClassMapSerializer<MutableClassMap>()

internal object TypedClassMapSerializer: AbstractTypedClassMapSerializer<TypedClassMap<Any>>()
internal object MutableTypedClassMapSerializer: AbstractTypedClassMapSerializer<MutableTypedClassMap<Any>>()

@OptIn(ExperimentalSerializationApi::class)
internal object MockAnySerializer: KSerializer<Any> by NothingSerializer().unsafeCast()