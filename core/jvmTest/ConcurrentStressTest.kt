package dev.dokky.dyno

import junit.framework.TestCase.assertFalse
import karamel.utils.unsafeCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.atomics.AtomicArray
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConcurrentStressTest {
    private companion object {
        val numWorkers = Runtime.getRuntime().availableProcessors()
        const val numIterations = 200_000
        const val numKeys = 10
    }

    private val keys = (0..<numKeys).map { i -> DynoKey<Int>("k$i") }
    private val noKey = DynoKey<String?>("unknown")

    private val newKeyEntry = DynoKey<Int>("plus") with 123

    private val obj = dynamicObjectOf(keys.mapIndexed { i, k -> DynoEntry(k, i) }).unsafeCast<DynamicObjectImpl>()
    private val encoded = Json.encodeToString<DynamicObject>(obj)

    @OptIn(ExperimentalAtomicApi::class)
    private fun test(serializer: KSerializer<DynamicObject>) {
        val objects = AtomicArray(numWorkers) {
            Json.decodeFromString(serializer, encoded).unsafeCast<DynamicObjectImpl>()
        }

        (1..numWorkers).map { workerIndex ->
            CompletableFuture.supplyAsync {
                repeat(numIterations) { iteration ->
                    testIteration(objects, serializer, workerIndex = workerIndex, iteration = iteration)
                }
            }
        }.forEach { it.join() }
    }

    @Test
    fun testSync() = test(DynamicObjectSerializer(DynoReadSafety.SYNCHRONIZED))

    @Test
    fun testNoCache() = test(DynamicObjectSerializer(DynoReadSafety.NO_CACHE))

    @OptIn(ExperimentalAtomicApi::class)
    private fun testIteration(
        objects: AtomicArray<DynamicObjectImpl>,
        serializer: KSerializer<DynamicObject>,
        workerIndex: Int,
        iteration: Int
    ) {
        var idx: Int
        do {
            idx = Random.nextInt(numWorkers)
        } while (idx == workerIndex)

        val acquired = objects.loadAt(idx)
        when (Random.nextInt(10)) {
            0 -> assertEquals(obj, acquired)
            1 -> assertEquals(acquired, obj)
            2 -> {
                for (i in 0 ..< numKeys) {
                    assertEquals(i, acquired[keys[i]])
                }
                repeat(3) {
                    assertNull(acquired[noKey])
                }
            }
            3 -> assertEquals(obj.size, acquired.size)
            4 -> assertEquals(obj.keyNames.toSet(), acquired.keyNames.toSet())
            5 -> assertEquals(obj.toString(), acquired.toString())
            6 -> {
                with(acquired) {
                    for (i in 0 ..< numKeys) {
                        val k = keys[i]
                        assertTrue(DynoMapBase.Unsafe.contains(k))
                    }
                    repeat(3) {
                        assertFalse(DynoMapBase.Unsafe.contains(noKey))
                    }
                }
            }
            7 -> assertEquals(obj + newKeyEntry, acquired + newKeyEntry)
            8 -> {
                val keyNum = Random.nextInt(numKeys)
                val key = DynoKey<Int>("k$keyNum")
                assertEquals(obj - key, acquired - key)
            }
            else -> {
                for (i in 0 ..< numKeys) {
                    val k = keys[i]
                    assert(k in acquired)
                }
                repeat(3) {
                    assert(noKey !in acquired)
                }
            }
        }

        objects.storeAt(idx, Json.decodeFromString(serializer, encoded).unsafeCast())
    }
}