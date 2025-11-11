package dyno

interface DynoSchema {
    val Internal.keys: Collection<DynoKey<*>>

    fun Internal.register(key: DynoKey<*>)

    object Internal
}