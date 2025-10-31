package dyno

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * Mutable version of [ClassMap] that allows adding, removing, and updating class-instance mappings.
 * The key must be a [KClass] corresponding to any serializable class.
 *
 * Note: While [MutableClassMap] itself is not directly serializable via kotlinx.serialization,
 * it can be serialized automatically when used as a property in a `@Serializable` class.
 *
 * Example of direct serialization (requires manual serializer):
 * ```
 * val classMap: MutableClassMap = buildMutableClassMap { put("value") }
 * val json = Json.encodeToString(MutableClassMapSerializer, classMap)
 * val restored = Json.decodeFromString(MutableClassMapSerializer, json)
 * ```
 *
 * When used as a property in @Serializable class, serializer is selected automatically:
 * ```
 * @Serializable
 * data class Container(val map: MutableClassMap)
 *
 * val container = Container(buildMutableClassMap { put("value") })
 * val json = Json.encodeToString(container)
 * val restored = Json.decodeFromString<Container>(json)
 * ```
 *
 * @see MutableClassMapSerializer
 */
typealias MutableClassMap = MutableTypedClassMap<@Serializable(MockAnySerializer::class) Any>

