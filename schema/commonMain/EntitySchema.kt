package dyno

import karamel.utils.unsafeCast
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

    final override fun getSerializer(): KSerializer<Entity<*>> =
        serializer ?: createSerializer().also { serializer = it }

    final override fun name(): String = name

    final override fun version(): Int = version

    fun <S: EntitySchema> serializer(): KSerializer<Entity<S>> = getSerializer().unsafeCast()
}

/**
 *
 * Marker interface indicating the [EntitySchema] is polymorphic base.
 * The marker is useless if schema is not [EntitySchema].
 *
 * Example:
 *
 * ```kotlin
 * // Base schema 'interface' does not have Polymorphic marker.
 * // It only declares base schema keys and should be abstract
 * sealed class BaseSchema(name: String): EntitySchema(name) {
 *     val baseKey by dynoKey<String>()
 *
 *     // notice the Polymorphic marker
 *     companion object: Vehicle("base"), Polymorphic
 * }
 *
 * object DerivedSchema1: BaseSchema("derived1")
 * object DerivedSchema2: BaseSchema("derived2")
 * ```
 */
interface Polymorphic: DynoSchema