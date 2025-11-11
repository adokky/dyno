package dyno

abstract class TestSchemaBase: DynoSchema {
    val keys = ArrayList<DynoKey<*>>()

    override val DynoSchema.Internal.keys: Collection<DynoKey<*>> get() = this@TestSchemaBase.keys

    override fun DynoSchema.Internal.register( key: DynoKey<*>) {
        this@TestSchemaBase.keys.add(key)
    }
}