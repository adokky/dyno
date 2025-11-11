package dyno

abstract class TestKeyRegistryBase: DynoKeyRegistry {
    val keys = ArrayList<DynoKey<*>>()

    override val DynoKeyRegistry.Internal.keys: Collection<DynoKey<*>> get() = this@TestKeyRegistryBase.keys

    override fun DynoKeyRegistry.Internal.register(key: DynoKey<*>) {
        this@TestKeyRegistryBase.keys.add(key)
    }
}