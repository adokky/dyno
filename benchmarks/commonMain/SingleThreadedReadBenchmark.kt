package dyno

import karamel.utils.toInt
import kotlinx.benchmark.*
import kotlinx.serialization.json.Json

@BenchmarkMode(Mode.Throughput)
@Warmup(15, time = 1000, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@Measurement(15, time = 1000, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
class SingleThreadedReadBenchmark {
    private companion object {
        const val numKeys = 10
    }

    private val keys = (0..<numKeys).map { i -> DynoKey<Int>("k$i") }
    private val noKey = DynoKey<String?>("unknown")

    private val obj = dynamicObjectOf(keys.mapIndexed { i, k -> DynoEntry(k, i) })
    private val encoded = Json.encodeToString(obj)

    private lateinit var decoded: DynamicObject
    private lateinit var decodedUnsafe: DynamicObject

    private val threadSafeSerializer = DynamicObjectSerializer(DynoReadSafety.SYNCHRONIZED)
    private val threadUnsafeSerializer = DynamicObjectSerializer(DynoReadSafety.UNSAFE)

    @Setup
    fun prepareDynamicObject() {
        decoded = Json.decodeFromString(threadSafeSerializer, encoded)
        decodedUnsafe = Json.decodeFromString(threadUnsafeSerializer, encoded)
    }

    @Benchmark
    fun singleThreadedUnsafe(): Int {
        return readAllKeys(decodedUnsafe)
    }

    @Benchmark
    fun singleThreadedSafe(): Int {
        return readAllKeys(decoded)
    }

    private fun readAllKeys(obj: DynamicObject): Int {
        var sum = 0

        repeat(numKeys) { keyIndex ->
            val key = keys[keyIndex]
            sum += obj[key]
        }

        repeat(3) {
            sum += (obj[noKey] == null).toInt()
        }

        return sum
    }
}

