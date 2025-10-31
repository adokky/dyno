package dyno

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder

/**
 * [DynoMapBase] with an optimized [AbstractDynoSerializer] that speeds up deserialization
 * by skipping the intermediate [kotlinx.serialization.json.JsonElement] step.
 *
 * Requires pre-registration of [DynoKeyProvider] in [SerializersModule].
 *
 * @see AbstractPolymorphicDynoSerializer
 * @see SerializersModuleBuilder.dynamicObjectKeys
 */
typealias PolymorphicDynoMap<K> = @Serializable(PolyDynoMapSerializer::class) DynoMap<K>

private class PolyDynoMapSerializer<K : DynoKey<*>>(baseKeySerializer: KSerializer<K>):
    AbstractPolymorphicDynoSerializerBase<DynoMap<K>>(baseKeySerializer)

/**
 * [DynamicObject] with an optimized [AbstractDynoSerializer] that speeds up deserialization
 * by skipping the intermediate [kotlinx.serialization.json.JsonElement] step.
 *
 * Requires pre-registration of [DynoKeyProvider] in [SerializersModule].
 *
 * @see AbstractPolymorphicDynoSerializer
 * @see SerializersModuleBuilder.dynamicObjectKeys
 */
typealias PolymorphicDynamicObject = @Serializable(PolyDynoSerializer::class) DynamicObject

private class PolyDynoSerializer(baseKeySerializer: KSerializer<*>):
    AbstractPolymorphicDynoSerializerBase<DynamicObject>(baseKeySerializer)

/**
 * [MutableDynamicObject] with an optimized [AbstractDynoSerializer] that speeds up deserialization
 * by skipping the intermediate [kotlinx.serialization.json.JsonElement] step.
 *
 * Requires pre-registration of [DynoKeyProvider] in [SerializersModule].
 *
 * @see AbstractPolymorphicDynoSerializer
 * @see SerializersModuleBuilder.dynamicObjectKeys
 */
typealias PolymorphicMutableDynamicObject = @Serializable(PolyMutableDynoSerializer::class) MutableDynamicObject

private class PolyMutableDynoSerializer(baseKeySerializer: KSerializer<*>):
    AbstractPolymorphicDynoSerializerBase<MutableDynamicObject>(baseKeySerializer)