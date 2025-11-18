package dyno

import kotlinx.serialization.KSerializer

abstract class EntitySchema(
    private val name: String,
    private val version: Int = 0,
    // accessing this, so must be initialized lazily
    private var serializer: KSerializer<Entity<*>>? = null
): AbstractDynoSchema<Entity<*>>() {
    protected open fun createSerializer(): KSerializer<Entity<*>> {
        return if (this@EntitySchema is Polymorphic) {
            PolymorphicEntitySerializer(name)
        } else {
            SchemaSerializer(this, PolymorphicDynoSerializer.DEFAULT_UNKNOWN_KEY_STRATEGY) { data, json ->
                MutableEntity(this@EntitySchema, data, json)
            }
        }
    }

    final override fun serializer(): KSerializer<Entity<*>> =
        serializer ?: createSerializer().also { serializer = it }

    final override fun name(): String = name

    final override fun version(): Int = version
}

interface Polymorphic