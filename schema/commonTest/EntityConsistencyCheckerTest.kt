package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.*

class EntityConsistencyCheckerTest {
    private class TestDynoSchema(private val keys: List<DynoKey<*>>) : DynoSchema {
        override fun name(): String = "TestSchema"
        override fun version(): Int = 0
        override fun getKey(serializersModule: SerializersModule, name: String) =
            error("not implemented")
        override fun keys(): List<DynoKey<*>> = keys
        override fun keyCount(): Int = keys.size
    }

    @Test
    fun all_required_fields_are_present() {
        val keys = listOf(
            DynoKey<Unit>("key1"),
            DynoKey<Unit>("key2"),
            DynoKey<Unit>("key3")
        )
        val schema = TestDynoSchema(keys)
        val checker = EntityConsistencyChecker(schema)

        var state: Any? = null
        for (key in keys) {
            state = checker.markIsPresent(state, key)
        }

        assertNull(checker.getRequiredKeysMissing(state))
    }

    @Test
    fun some_required_fields_are_missing() {
        val keys = listOf(
            DynoKey<Unit>("key1"),
            DynoKey<Unit>("key2"),
            DynoKey<Unit?>("key3"),
            DynoKey<Unit>("key4")
        )
        val schema = TestDynoSchema(keys)
        val checker = EntityConsistencyChecker(schema)

        // Mark only key2 and key3 as present
        var state: Any? = null
        state = checker.markIsPresent(state, keys[1]) // key2
        state = checker.markIsPresent(state, keys[2]) // key3 (optional)

        val missing = checker.getRequiredKeysMissing(state)
        assertNotNull(missing)
        assertEquals(2, missing.size)
        assertTrue("key1" in missing)
        assertTrue("key4" in missing)
    }

    @Test
    fun no_required_fields() {
        val keys = listOf(
            DynoKey<Unit?>("key1"),
            DynoKey<Unit?>("key2")
        )
        val schema = TestDynoSchema(keys)
        val checker = EntityConsistencyChecker(schema)

        val missing = checker.getRequiredKeysMissing(null)
        assertNull(missing)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun check_with_missing_fields() {
        val keys = listOf(
            DynoKey<Unit>("requiredKey")
        )
        val schema = TestDynoSchema(keys)
        val checker = EntityConsistencyChecker(schema)

        assertFailsWith<MissingFieldException> {
            checker.check(null)
        }.let { exception ->
            assertEquals(1, exception.missingFields.size)
            assertEquals("requiredKey", exception.missingFields[0])
        }
    }

    @Test
    fun int_based_tracking() {
        // Test with <= 32 keys to use Int-based tracking
        val ser = Unit.serializer()
        val nser = ser.nullable
        val keys = (1..20).map { DynoKey<Unit>("key$it", serializer = (if (it % 2 == 0) nser else ser).unsafeCast()) }
        val schema = TestDynoSchema(keys)
        val checker = EntityConsistencyChecker(schema)

        var state: Any? = null
        // Mark only even (optional) keys as present
        for (i in keys.indices) {
            if (i % 2 == 1) { // odd indices are even numbers (0-based)
                state = checker.markIsPresent(state, i)
            }
        }

        val missing = checker.getRequiredKeysMissing(state)
        assertNotNull(missing)
        assertEquals(10, missing.size)
        for (i in 0 until 20 step 2) {
            assertTrue("key${i+1}" in missing) // 1-based key names
        }
    }

    @Test
    fun bit_vector_based_tracking() {
        // Test with > 32 keys to use BitVector-based tracking
        val ser = Unit.serializer()
        val nser = ser.nullable
        val keys = (1..50).map { DynoKey<Unit>("key$it", serializer = (if (it % 3 == 0) nser else ser).unsafeCast()) }
        assertEquals(16, keys.count { it.isOptional })
        val schema = TestDynoSchema(keys)
        val checker = EntityConsistencyChecker(schema)

        var state: Any? = null
        // Mark only keys divisible by 3 as present (which are optional)
        for (i in keys.indices) {
            if ((i + 1) % 3 == 0) { // 1-based indexing for key names
                state = checker.markIsPresent(state, i)
            }
        }

        val missing = checker.getRequiredKeysMissing(state)
        assertNotNull(missing)
        // Should have 34 missing keys (50 total - 16 optional = 34 required, 0 present)
        assertEquals(34, missing.size)
    }

    @Test
    fun mark_is_present_with_index() {
        val keys = listOf(DynoKey<Unit>("key1"), DynoKey<Unit>("key2"))
        val schema = TestDynoSchema(keys)
        val checker = EntityConsistencyChecker(schema)

        // Test marking with index
        var state: Any? = null
        state = checker.markIsPresent(state, 0)

        val missing = checker.getRequiredKeysMissing(state)
        assertNotNull(missing)
        assertEquals(1, missing.size)
        assertEquals("key2", missing[0])
    }
}
