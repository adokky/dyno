package dev.dokky.dyno

import kotlinx.benchmark.*
import kotlinx.serialization.json.Json
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.atomics.AtomicArray
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.random.Random

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(15, batchSize = 1)
@Measurement(15, batchSize = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
class ConcurrentReadBenchmark {
    private companion object {
        val numWorkers = Runtime.getRuntime().availableProcessors()
        const val numIterations = 100_000
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

    @Benchmark
    fun concurrent(): Any {
        return (1..numWorkers).map { workerIndex ->
            CompletableFuture.supplyAsync {
                val random = Random(87432 + workerIndex)
                repeat(numIterations) { iteration ->
                    testIteration(workerIndex, iteration, random)
                }
            }
        }.map { it.get() }
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun testIteration(workerIndex: Int, iteration: Int, random: Random) {
        var idx: Int
        do {
            idx = random.nextInt(numWorkers)
        } while (idx == workerIndex)

        val acquired = objects.loadAt(idx)

        for (i in 0 ..< numKeys) {
            check(i == acquired[keys[i]])
        }
        check(acquired[noKey] == null)

        objects.storeAt(idx, Json.decodeFromString<DynamicObject>(encoded))
    }
}