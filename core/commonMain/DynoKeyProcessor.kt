package dev.dokky.dyno

import kotlin.jvm.JvmName

/**
 * A functional interface representing a processor for [DynoKey] values of type [T].
 *
 * Processors are designed to be composed into chains using the [plus] operator.
 * When a key is processed, all processors in the chain are executed in order.
 */
@DynoDslMarker
fun interface DynoKeyProcessor<in T: Any> {
    /**
     * Processes the given value in the context of the [DynoKey]
     * that this processor is associated with.
     *
     * This method is called when a key is processed, typically during decoding or assignment.
     */
    fun DynoKey<*>.process(value: T)
}

/**
 * Combines two [DynoKeyProcessor] instances into a single processor chain.
 *
 * If either operand is `null`, the other is returned.
 * Otherwise, a new processor chain is created containing both processors.
 */
@JvmName("nullablePlus")
internal operator fun <T: Any> DynoKeyProcessor<T>?.plus(other: DynoKeyProcessor<T>): DynoKeyProcessor<T> =
    when(this) {
        null -> other
        else -> this + other
    }

/**
 * Combines two [DynoKeyProcessor] instances into a single processor chain.
 *
 * Creates a new processor chain containing both processors.
 */
operator fun <T: Any> DynoKeyProcessor<T>.plus(other: DynoKeyProcessor<T>): DynoKeyProcessor<T> =
    DynoKeyProcessorChain(arrayOf(this, other))

/**
 * Retrieves the sub-processors of this [DynoKeyProcessor]
 * if it was created using [DynoKeyProcessor.plus].
 *
 * Returns `null` if this processor is not a chain.
 *
 * @see DynoKeyProcessor.plus
 */
@ExperimentalDynoApi
fun <T: Any> DynoKeyProcessor<T>.subProcessors(): List<DynoKeyProcessor<T>>? =
    (this as? DynoKeyProcessorChain<T>)?.processors?.toList()


internal class DynoKeyProcessorChain<T: Any>(
    processors: Array<out DynoKeyProcessor<T>>
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