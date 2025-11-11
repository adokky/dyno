package dyno

import dev.dokky.bitvector.BitVector
import dev.dokky.bitvector.MutableBitVector
import dev.dokky.bitvector.bitsOf
import karamel.utils.MutableInt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException

internal class EntityConsistensyChecker(private val schema: DynoSchema) {
    private val nameToIndex: HashMap<String, Int>? =
        if (schema is AbstractDynoSchema<*>) null else {
            HashMap<String, Int>(schema.keyCount()).also { mapping ->
                var index = 0
                for (k in schema.keys()) {
                    mapping[k.name] = index++
                }
            }
        }

    private val requiredFieldsMask: Any = if(schema.keyCount() > 32) {
        MutableBitVector(schema.keyCount()).also { bits ->
            for (k in schema.keys()) {
                val index = getPropertyIndex(k)
                if (!k.isOptional) bits.set(index)
            }
        }
    } else {
        var mask = 0
        for (k in schema.keys()) {
            val index = getPropertyIndex(k)
            if (!k.isOptional) mask = mask or (1 shl index)
        }
        mask
    }

    private fun getPropertyIndex(key: DynoKey<*>): Int =
        when (key) {
            is SchemaProperty<*, *> -> key.index
            else -> nameToIndex!!.getValue(key.name)
        }

    fun markIsPresent(state: Any?, index: Int): Any {
        when (state) {
            null -> return when {
                schema.keyCount() > 32 -> bitsOf(index)
                else -> MutableInt(1 shl index)
            }
            is MutableInt -> state.value = state.value or (1 shl index)
            else -> (state as MutableBitVector).set(index)
        }
        return state
    }

    fun markIsPresent(state: Any?, key: DynoKey<*>): Any {
        return markIsPresent(state, getPropertyIndex(key))
    }

    private fun Int.isPropertyPresent(index: Int): Boolean = (this and (1 shl index)) != 0

    fun getRequiredKeysMissing(state: Any?): List<String>? = when (state) {
        null -> schema.keys().let { keys ->
            ArrayList<String>(keys.size).apply {
                for (k in keys) {
                    if (!k.isOptional) add(k.name)
                }
            }.takeUnless { it.isEmpty() }
        }
        is MutableInt -> when {
            state.value and (requiredFieldsMask as Int) == requiredFieldsMask -> null
            else -> getMissingFields(state.value)
        }
        else -> {
            state as BitVector
            when {
                state and (requiredFieldsMask as BitVector) == requiredFieldsMask -> null
                else -> getMissingFields(state)
            }
        }
    }

    fun check(finalState: Any?) {
        val missing = getRequiredKeysMissing(finalState)
        if (!missing.isNullOrEmpty()) {
            @OptIn(ExperimentalSerializationApi::class)
            throw MissingFieldException(missing, serialName = schema.name())
        }
    }

    private fun getMissingFields(state: BitVector) = buildList<String> {
        for (k in schema.keys()) {
            val index = getPropertyIndex(k)
            if (!state[index] && !k.isOptional) add(k.name)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getMissingFields(state: Int): ArrayList<String> {
        requiredFieldsMask as Int
        val mask = requiredFieldsMask.inv() or state
        val missingCount = mask.inv().countOneBits()
        return ArrayList<String>(missingCount).apply {
            for (k in schema.keys()) {
                if (1.shl(getPropertyIndex(k)).and(mask) == 0) {
                    add(k.name)
                }
            }
        }
    }
}