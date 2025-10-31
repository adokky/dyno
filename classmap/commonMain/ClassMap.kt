package dyno

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * A container for mapping [KClass] instances to values.
 * The key must be a [KClass] corresponding to any serializable class.
 *
 * Note: While [ClassMap] itself is not directly serializable via kotlinx.serialization,
 * it can be serialized automatically when used as a property in a `@Serializable` class.
 *
 * Example of direct serialization (requires manual serializer):
 * ```
 * val classMap: ClassMap = buildClassMap { put("value") }
 * val json = Json.encodeToString(ClassMapSerializer, classMap)
 * val restored = Json.decodeFromString(ClassMapSerializer, json)
 * ```
 *
 * When used as a property in @Serializable class, serializer is selected automatically:
 * ```
 * @Serializable
 * data class Container(val map: ClassMap)
 *
 * val container = Container(buildClassMap { put("value") })
 * val json = Json.encodeToString(container)
 * val restored = Json.decodeFromString<Container>(json)
 * ```
 *
 * @see ClassMapSerializer
 */
typealias ClassMap = TypedClassMap<@Serializable(MockAnySerializer::class) Any>