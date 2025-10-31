package dyno

import kotlinx.serialization.KSerializer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class SchemaProperty<R: DynoSchema, T: Any>(
    name: String,
    serializer: KSerializer<T>
) : SimpleDynoKey<T>(name, serializer),
    ReadOnlyProperty<R, SchemaProperty<R, T>>
{
    @ExperimentalStdlibApi
    override fun getValue(thisRef: R, property: KProperty<*>): SchemaProperty<R, T> = this
}