package dyno

import kotlin.test.Test
import kotlin.test.assertEquals

class DynoKeyProcessorTest {
    private val k = DynoKey<Unit>("key")

    @Test
    fun chaining() {
        val seq = ArrayList<Int>()

        val p1 = DynoKeyProcessor<Unit> { seq += 1 }
        val p2 = DynoKeyProcessor<Unit> { seq += 2 }
        val p3 = DynoKeyProcessor<Unit> { seq += 3 }

        with(null + p1 + p2 + p3) { k.process(Unit) }

        assertEquals(listOf(1, 2, 3), seq)

        with(null + p1 + p2 + p3) { k.process(Unit) }

        assertEquals(listOf(1, 2, 3, 1, 2, 3), seq)
    }

    @Test
    fun decode_callback() {
        val history = ArrayList<Int>()
        fun assertEmptyHistory() = assertEquals(emptyList(), history)

        val k = DynoKey<Int>("key")
            .onDecode { history.add(it) }
            .onDecode { history.add(it * 10) }
            .onDecode { history.add(-it) }

        val m = mutableDynamicObjectOf(k with 12)
        assertEmptyHistory()
        m[k]
        assertEmptyHistory()
        m[k] = 42
        assertEmptyHistory()

        val decoded = m.encodeDecode()
        assertEmptyHistory()

        decoded[k]
        assertEquals(listOf(42, 420, -42), history)
        history.clear()

        decoded.put(k with 56)
        assertEmptyHistory()
    }

    @Test
    fun assign_callback() {
        val history = ArrayList<Int>()
        fun assertEmptyHistory() = assertEquals(emptyList(), history)

        val k = DynoKey<Int>("key")
            .onAssign { history.add(it) }
            .onAssign { history.add(it * 10) }
            .onAssign { history.add(-it) }

        val m = mutableDynamicObjectOf(k with 12)
        assertEquals(listOf(12, 120, -12), history)
        history.clear()

        m[k]
        assertEmptyHistory()

        m[k] = 42
        assertEquals(listOf(42, 420, -42), history)
        history.clear()

        m.put(k, 56)
        assertEquals(listOf(56, 560, -56), history)
        history.clear()

        val decoded = m.encodeDecode()
        assertEmptyHistory()

        decoded[k]
        assertEmptyHistory()
        decoded.put(k with 37)
        assertEquals(listOf(37, 370, -37), history)
    }

    @Test
    fun callback_order() {
        val p1 = DynoKeyProcessor<Unit> {}
        val p2 = DynoKeyProcessor<Unit> {}
        val p3 = DynoKeyProcessor<Unit> {}

        val k = DynoKey<Unit>("key")
            .onDecode(p1)
            .validate(p2)
            .onAssign(p3)

        assertEquals(
            listOf(p1, p2),
            (k.onDecode as DynoKeyProcessorChain).processors.toList()
        )

        assertEquals(
            listOf(p2, p3),
            (k.onAssign as DynoKeyProcessorChain).processors.toList()
        )
    }

    @Test
    fun validate_callback() {
        val history = ArrayList<Int>()
        fun assertEmptyHistory() = assertEquals(emptyList(), history)

        val k = DynoKey<Int>("key")
            .onDecode { history.add(it) }
            .validate { history.add(it * 10) }
            .onAssign { history.add(-it) }

        val m = mutableDynamicObjectOf(k with 12)
        assertEquals(listOf(120, -12), history)
        history.clear()

        m[k]
        assertEmptyHistory()

        m[k] = 42
        assertEquals(listOf(420, -42), history)
        history.clear()

        m.put(k, 56)
        assertEquals(listOf(560, -56), history)
        history.clear()

        val decoded = m.encodeDecode()
        assertEmptyHistory()

        decoded.put(k, 111)
        assertEquals(listOf(1110, -111, 56, 560), history)
        history.clear()

        decoded.put(k with 37)
        assertEquals(listOf(370, -37), history)
    }
}