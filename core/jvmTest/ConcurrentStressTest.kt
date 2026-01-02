package dyno

import kotlinx.serialization.json.Json
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.atomics.AtomicArray
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConcurrentStressTest {
    private companion object {
        val numWorkers = Runtime.getRuntime().availableProcessors()
        const val numIterations = 1_000_000
        const val numKeys = 10
    }

    private val keys = (0..<numKeys).map { i -> DynoKey<Int>("k$i") }
    private val noKey = DynoKey<String?>("unknown")

    private val obj = dynamicObjectOf(keys.mapIndexed { i, k -> DynoEntry(k, i) })
    private val encoded = Json.encodeToString(obj)

    @OptIn(ExperimentalAtomicApi::class)
    private val objects = AtomicArray(numWorkers) {
        Json.decodeFromString<DynamicObject>(encoded)
    }

    @Test
    fun test() {
        (1..numWorkers).map { workerIndex ->
            CompletableFuture.supplyAsync {
                repeat(numIterations) { iteration ->
                    testIteration(workerIndex, iteration)
                }
            }
        }.forEach { it.join() }
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun testIteration(workerIndex: Int, iteration: Int) {
        var idx: Int
        do {
            idx = Random.nextInt(numWorkers)
        } while (idx == workerIndex)

        val acquired = objects.loadAt(idx)
        when (Random.nextInt(4)) {
            0 -> assertEquals(obj, acquired)
            1 -> assertEquals(acquired, obj)
            2 -> {
                for (i in 0 ..< numKeys) {
                    assertEquals(i, acquired[keys[i]])
                }
                assertNull(acquired[noKey])
            }
            else -> {
                for (i in 0 ..< numKeys) {
                    val k = keys[i]
                    assert(k in acquired)
                }
                assert(noKey !in acquired)
            }
        }

        objects.storeAt(idx, Json.decodeFromString<DynamicObject>(encoded))
    }
}