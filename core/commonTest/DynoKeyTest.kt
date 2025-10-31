package dyno

import dev.adokky.eqtester.testEquality
import kotlin.test.Test
import kotlin.test.assertEquals

class DynoKeyTest {
    @Test
    fun auto_equality_test() {
        testEquality {
            group { DynoKey<String>("k1") }
            group { DynoKey<String>("k2") }
            group { DynoRequiredKey<String>("k3") }
            group(
                DynoKey<String>("k4"),
                DynoRequiredKey<Int>("k4"),
                DynoKey<List<String>>("k4"),
                SimpleDynoRequiredKey<Short>("k4"),
                SimpleDynoKey<Map<Boolean, Boolean>>("k4"),
            )
            checkToString = true
            requireNonIdentical = true
        }
    }

    @Test
    fun to_string() {
        assertEquals("xyz", DynoKey<Int>("xyz").toString())
    }
}