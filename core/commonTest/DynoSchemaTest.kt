package dyno

import kotlin.test.Test
import kotlin.test.assertEquals

class DynoSchemaTest {
    object TestSchema: TestSchemaBase() {
        val k1 by dynoKey<String>()
        val k2 by dynoKey<Int?>()
        val k3 by dynoKey<Int>().validate { println(name + it) }.optional()
    }

    val TestSchema.k4 by dynoKey<String>()

    @Test
    fun all_member_keys_should_be_registered() {
        repeat(2) {
            assertEquals(
                setOf(TestSchema.k1, TestSchema.k2, TestSchema.k3),
                TestSchema.keys.toSet()
            )
        }
    }
}