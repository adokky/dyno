package dyno

import dev.adokky.eqtester.EqualsTesterConfigBuilder
import dev.adokky.eqtester.testEquality
import dyno.DynoMapBase.Unsafe
import karamel.utils.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

open class EqualityTest: AbstractDynoTest() {
    private val p4 = DynoKey<String>("p4")
    private val p5 = DynoKey<String>("p5")
    private val p6 = DynoKey<String>("p6")

    private fun <K: DynoKey<*>> EqualsTesterConfigBuilder.dynoGroup(vararg entries: DynoEntry<K, *>) {
        val do0 = dynamicObjectOf(*entries)
        val do1 = do0.encodeDecode()
        val do2 = do0.copy()

        group(
            do0,
            dynamicObjectOf(*entries),
            dynamicObjectOf(*entries).encodeDecode(),
            do2,
            do2.encodeDecode(),
            do1,
            do1.encodeDecode(),
            do1.copy().apply { Unsafe.get(p1) }, // partially decode
            do0.copy()
                .unsafeCast<DynamicObjectImpl>()
                .apply { zeroEffectMutations() }
        )
    }

    private fun DynamicObjectImpl.zeroEffectMutations() {
        hashCode()

        with(Unsafe) {
            set(p4, "hello")
            set(p5, "world")

            put(p4, "A")
            remove(p4)

            put(p4, "B")
            remove(p4)

            put(p4, "B")
            removeAndGet(p4)

            removeAndGet(p5)

            put(p6, "v")
            removeAndGet(p6)
        }
    }

    @Test
    fun auto() {
        testEquality {
            dynoGroup(p1.with("string"))
            dynoGroup(p2.with(123))
            dynoGroup(p2.with(345))
            dynoGroup(pListOfInts.with(listOf(1, 2, 3)))
            dynoGroup(pListOfInts.with(listOf(3, 4, 5)))
            dynoGroup(p1.with("s1"))
            dynoGroup(p1.with("s1"), p2.with(123))
            dynoGroup(p1.with("s1"), p2.with(123), pListOfInts.with(listOf()))
            dynoGroup(p1.with("s1"), p2.with(123), pListOfInts.with(listOf(1, 2, 3)))
            group(dynamicObjectOf(), dynamicObjectOf().encodeDecode(), DynamicObjectImpl(HashMap(), null))
            requireNonIdentical = true
        }
    }

    @Test
    fun empty() {
        assertEquals(dynamicObjectOf(), dynamicObjectOf())

        assertEquals(dynamicObjectOf(), dynamicObjectOf(emptyList()))
        assertEquals(dynamicObjectOf(emptyList()), dynamicObjectOf())
        assertEquals(dynamicObjectOf(emptyList()), dynamicObjectOf(emptyList()))

        assertEquals(DynamicObjectImpl(2), dynamicObjectOf())
        assertEquals(dynamicObjectOf(), DynamicObjectImpl(2))
    }

    @Test
    fun simple() {
        var m1 = mutableDynamicObjectOf(e1, e2, e3) as DynamicObjectImpl
        var m2 = mutableDynamicObjectOf(e3, e1, e2, e3) as DynamicObjectImpl

        fun check(m: DynoMapImpl) {
            listOf(e1, e2, e3).forEach { entry ->
                assertEquals(entry.value, with(m) { Unsafe.get(entry.key) })
            }
        }

        check(m1)
        check(m2)

        assertEquals(m1, m2)
        assertEquals(m2, m1)

        m1 = m1.encodeDecode()

        assertEquals(m1, m2)
        assertEquals(m2, m1)

        m1 = m1.encodeDecode()
        m2 = m2.encodeDecode()

        assertEquals(m1, m2)
        assertEquals(m2, m1)

        m1 = m1.encodeDecode()
        m2 = m2.encodeDecode()

        assertEquals(m2, m1)
        assertEquals(m1, m2)
    }

    @Test
    fun complex() {
        var m1 = mutableDynamicObjectOf(e1, e2, e3) as DynamicObjectImpl
        var m2 = mutableDynamicObjectOf(e3, e1, e2, e3) as DynamicObjectImpl

        fun check(action: (DynamicObjectImpl) -> Unit) {
            action(m1)
            m2 = m2.encodeDecode()
            action(m2)
            assertEquals(m1, m2)
            assertEquals(m2, m1)

            action(m2)
            m1 = m1.encodeDecode()
            action(m1)
            assertEquals(m1, m2)
            assertEquals(m2, m1)
        }

        check { it.remove(e1.key) }
        check { it.put(e2.key, 104) }
        check { it[e3.key] = listOf(false) }
        check { it[e3.key] }
    }

    @Test
    fun partially_decoded_equal() {
        fun m1() = dynamicObjectOf(e1, e2, e3)
        fun m2() = m1().encodeDecode()
        fun m3() = m2().also { it.getOrFail(e1.key) }
        fun m4() = m2().also { it.getOrFail(e2.key) }

        val maps = listOf(::m1, ::m2, ::m3, ::m4)

        for (i1 in maps.indices) {
            for (i2 in maps.indices) {
                assertEquals(maps[i1](), maps[i2]())
            }
        }
    }

    @Test
    fun partially_decoded_not_equal() {
        fun m1() = dynamicObjectOf(e1, e2, e3)
        fun m2() = m1().encodeDecode() - e3.key
        fun m3() = m2().also { it.getOrFail(e1.key) } + (p1 with "X")
        fun m4() = m2().also { it.getOrFail(e2.key) } + (p2 with 6673)
        fun m5() = m2() - e1.key
        fun m6() = m2() - e2.key
        fun m7() = (m2() + (p1 with "Y")).encodeDecode()

        val maps = listOf(::m1, ::m2, ::m3, ::m4, ::m5, ::m6, ::m7)

        for (i1 in maps.indices) {
            for (i2 in maps.indices) {
                val m1 = maps[i1]()
                val m2 = maps[i2]()
                if (i1 == i2) {
                    assertEquals(m1, m2)
                } else {
                    assertNotEquals(m1, m2, "$m1 $m2")
                }
            }
        }
    }
}