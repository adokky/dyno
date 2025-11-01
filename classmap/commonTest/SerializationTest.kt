package dyno

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest: AbstractClassMapTest() {
    @Test
    fun holder_mutable() {
        @Serializable data class Holder(val cm: MutableClassMap)
        val holder = Holder(buildMutableClassMap { put("zzz"); put(111) })
        val str = Json.encodeToString(holder)
        val decoded = Json.decodeFromString<Holder>(str)
        assertEquals(holder, decoded)
    }

    @Test
    fun holder_read_only() {
        @Serializable data class Holder(val cm: ClassMap)
        val holder = Holder(buildClassMap { put("zzz"); put(111) })
        val str = Json.encodeToString(holder)
        val decoded = Json.decodeFromString<Holder>(str)
        assertEquals(holder, decoded)
    }

    @Test
    fun mutable() {
        val value = buildMutableTypedClassMap<Any> { put("zzz"); put(111) }
        val str = Json.encodeToString(MutableTypedClassMapSerializer, value)
        val decoded = Json.decodeFromString(MutableTypedClassMapSerializer, str)
        assertEquals(value, decoded)
    }

    @Test
    fun read_only() {
        val value = buildTypedClassMap<Any> { put("zzz"); put(111) }
        val str = Json.encodeToString(TypedClassMapSerializer, value)
        val decoded = Json.decodeFromString(TypedClassMapSerializer, str)
        assertEquals(value, decoded)
    }
}