package dyno

import karamel.utils.assert
import kotlinx.serialization.KSerializer

abstract class EntitySchema(
    private val name: String,
    private var serializer: KSerializer<Entity<*>>? = null
): AbstractDynoSchema<Entity<*>>() {
    // accessing this, so must be initialized lazily
    private fun initSerializer(): KSerializer<Entity<*>> {
        assert { serializer == null }
        val result = if (this@EntitySchema is Polymorphic) {
            PolymorphicEntitySerializer(name)
        } else {
            SchemaSerializer<Entity<*>>(this, UnknownKeysStrategy.KeepIfJsonAllowed) { data, json ->
                MutableEntity(this@EntitySchema, data, json)
            }
        }
        serializer = result
        return result
    }

    final override fun serializer(): KSerializer<Entity<*>> = serializer ?: initSerializer()

    final override fun name(): String = name
}

interface Polymorphic