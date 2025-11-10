package dyno

import kotlinx.serialization.serializer

abstract class DynoSchema() {
    internal fun register(key: DynoKey<*>) {

    }

    // todo register keys here
    protected inline fun <reified T: Any> dynoKey(): DynoKeyPrototype<T> =
        DynoKeyPrototype(serializer<T>())
}