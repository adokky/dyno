package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.json.Json

internal sealed class AbstractClassMapSerializer<T: TypedClassMap<*>>: AbstractDynoSerializer<T>() {
    override fun createMap(data: HashMap<Any, Any>?, json: Json?): T =
        MutableTypedClassMap<Any>(data, json).unsafeCast()
}

internal object ClassMapSerializer: AbstractClassMapSerializer<TypedClassMap<Any>>()

internal object MutableClassMapSerializer: AbstractClassMapSerializer<MutableTypedClassMap<Any>>()

@OptIn(ExperimentalSerializationApi::class)
internal object MockAnySerializer: KSerializer<Any> by NothingSerializer().unsafeCast()