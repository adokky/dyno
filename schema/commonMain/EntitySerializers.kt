package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer

sealed class AbstractEntitySerializer<E: Entity<S>, S : TypedDynoSchema<S>>(
    private val schemaSerializer: KSerializer<S>
): KSerializer<E> by getEntitySerializer(schemaSerializer)

private fun <E : Entity<S>, S : TypedDynoSchema<S>> getEntitySerializer(schemaSerializer: KSerializer<S>): KSerializer<E> {
    var schema: KSerializer<*> = schemaSerializer
    schema = (schema as? DynoSchemaSerializer<*>)?.actualSchema ?: schemaSerializer
    schema = schema as? TypedDynoSchema<*>
        ?: error("default serializer for Entity only accepts TypedDynoSchema or DynoSchemaSerializer " +
            "as a serializer for schema")
    return schema.unsafeCast()
}

class EntitySerializer<S : TypedDynoSchema<S>>(schemaSerializer: KSerializer<S>):
    AbstractEntitySerializer<Entity<S>, S>(schemaSerializer)

class MutableEntitySerializer<S : TypedDynoSchema<S>>(schemaSerializer: KSerializer<S>):
    AbstractEntitySerializer<MutableEntity<S>, S>(schemaSerializer)