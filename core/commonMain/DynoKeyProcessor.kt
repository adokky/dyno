package dyno

import kotlin.jvm.JvmName

@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class DynoDslMarker

@DynoDslMarker
fun interface DynoKeyProcessor<in T: Any> {
    fun DynoKey<*>.process(value: T)
}

@JvmName("nullablePlus")
internal operator fun <T: Any> DynoKeyProcessor<T>?.plus(other: DynoKeyProcessor<T>): DynoKeyProcessor<T> =
    when(this) {
        null -> other
        else -> this + other
    }

operator fun <T: Any> DynoKeyProcessor<T>.plus(other: DynoKeyProcessor<T>): DynoKeyProcessor<T> =
    DynoKeyProcessorChain(this, other)

internal class DynoKeyProcessorChain<T: Any>(
    vararg processors: DynoKeyProcessor<T>
): DynoKeyProcessor<T> {
    init {
        require(processors.isNotEmpty())
    }

    val processors: Array<out DynoKeyProcessor<T>>

    init {
        // Assuming that most of the time there is either no chain in the processor list or only a single one.
        // This aligns with the behavior of onDecode, onAssign, and validate extensions of DynoKey.
        val firstChain = processors.firstOrNull { it is DynoKeyProcessorChain } as? DynoKeyProcessorChain
        this.processors = if (firstChain == null) processors else {
            buildList(firstChain.processors.size + processors.size - 1) {
                for (p in processors) {
                    when (p) {
                        is DynoKeyProcessorChain -> addAll(p.processors)
                        else -> add(p)
                    }
                }
            }.toTypedArray()
        }
    }

    override fun DynoKey<*>.process(value: T) {
        this@DynoKeyProcessorChain.processors.forEach { processor ->
            with(processor) {
                this@process.process(value)
            }
        }
    }
}