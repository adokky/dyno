package dyno

import dev.dokky.pool.AbstractObjectPool
import dyno.AbstractEagerDynoSerializer.BaseResolveContext
import dyno.AbstractEagerDynoSerializer.ResolveContext
import karamel.utils.ThreadLocal
import karamel.utils.unsafeCast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer

/**
 * Optimized deserializer that skips intermediate [JsonElement] step.
 * Subclasses must implement [resolve].
 */
abstract class AbstractEagerDynoSerializer<T: DynoMapBase>: AbstractDynoSerializer<T>() {
    /**
     * Result of key resolution during deserialization.
     * Take a note that [DynoKey] is a subclass of [ResolveResult].
     */
    sealed interface ResolveResult {
        /** Skip this key and its value entirely. */
        object Skip: ResolveResult
        /** Keep this key and its value in the resulting map as [JsonElement] */
        object Keep: ResolveResult
        /**
         * Delay processing of this key until after the [resolve] stage.
         * Useful for keys that depend on other keys.
         */
        object Delay: ResolveResult
    }

    /**
     * Context provided to the [resolve] and [postResolve] methods.
     */
    sealed interface BaseResolveContext {
        val json: Json

        /** The key currently being processed. */
        val keyString: String

        /**
         * Retrieves [JsonElement] value for the [key] that was not recognized by the resolver.
         * - Does NOT include the [keyString] at [resolve] stage.
         * - DOES include the [keyString] at [postResolve] stage, if it was not skipped at [resolve].
         * - DOES include delayed keys.
         */
        fun getScannedUnknown(key: String): JsonElement?

        /**
         * Checks if an unknown [key] has been scanned.
         * - Does NOT include the [keyString] at [resolve] stage.
         * - MAY include the [keyString] at [postResolve] stage.
         * - DOES include delayed keys.
         */
        fun isScannedUnknown(key: String): Boolean

        /** Tries to get a value of an already decoded key. */
        fun <T: Any> getDecoded(key: DynoKey<T>): T?

        /**
         * Decodes the value for the current key as [JsonElement].
         * Not recommended to invoke after [decodeValue].
         */
        fun getJsonValue(): JsonElement

        /**
         * Decodes the value for the current key using the provided serializer.
         * Performance concern: this function is not recommended to
         * use with [ResolveResult.Keep] or [ResolveResult.Delay] [resolve] results,
         * because it may provoke double deserialization of the key.
         */
        @ExperimentalDynoApi
        fun <T: Any> decodeValue(serializer: KSerializer<T>): T?
    }
    
    sealed interface ResolveContext: BaseResolveContext {
        /** User-defined temporal state available only during the deserialization process */
        var state: Any?
    }

    protected open fun initContextState(json: Json): Any? = null

    /**
     * Resolve [DynoKey] given current [ResolveContextImpl].
     */
    protected abstract fun resolve(context: ResolveContext): ResolveResult

    /**
     * Resolve [DynoKey] given current [ResolveContextImpl].
     * Called after all keys have been scanned.
     */
    protected open fun postResolve(context: ResolveContext): ResolveResult = ResolveResult.Skip

    /**
     * @property delayedKeys *current* unknown keys.
     * - Does NOT include the [keyString] at initial deserialization stage.
     * - DOES include [keyString] at post-deserialization stage.
     */
    @InternalDynoApi
    protected open class ResolveContextImpl : ResolveContext, AutoCloseable {
        private var data: Map<Any, Any> = emptyMap()

        private var delayedKeys: HashMap<String, JsonElement> = HashMap()
        private var delayedKeysIterator: Iterator<Map.Entry<String, JsonElement>>? = null
        private var delayedKeysCapacity = 0

        final override var json: Json = Json
            private set
        protected var decoder: CompositeDecoder? = null
            private set
        private var descriptor: SerialDescriptor = Unit.serializer().descriptor

        final override var keyString: String = ""
            private set
        private var valueIndex: Int = 0
        private var valueSerializer: KSerializer<Any>? = null
        private var decodedValue: Any? = null
        private var jsonElement: JsonElement? = null

        final override var state: Any? = null

        override fun close() {
            state = null
            decoder = null
            jsonElement = null
            decodedValue = null
            valueSerializer = null
            delayedKeysIterator = null
            data = emptyMap()
            json = Json
            descriptor = Unit.serializer().descriptor
            keyString = ""
            if (delayedKeysCapacity > 512) {
                delayedKeys = HashMap()
                delayedKeysCapacity = 0
            } else {
                delayedKeys.clear()
            }
        }

        protected open val valueIsRead: Boolean get() = valueSerializer != null || jsonElement != null

        final override fun getJsonValue(): JsonElement {
            valueSerializer?.let { serializer ->
                return json.encodeToJsonElement(serializer, decodedValue!!).also {
                    jsonElement = it
                }
            }
            return jsonElement ?: decodeJsonElement().also { jsonElement = it }
        }

        open fun skipValue() { if (valueSerializer == null) getJsonValue() }

        final override fun getScannedUnknown(key: String): JsonElement? =
            (getDelayedKey(key) ?: data[key]).unsafeCast()

        final override fun isScannedUnknown(key: String): Boolean =
            isDelayedKeyExist(key) || key in data

        protected open fun getDelayedKey(key: String): JsonElement? =
            delayedKeys[key]

        protected open fun isDelayedKeyExist(key: String): Boolean =
            key in delayedKeys

        final override fun <T : Any> getDecoded(key: DynoKey<T>): T? = data[key].unsafeCast()

        open fun prepare(
            descriptor: SerialDescriptor,
            json: Json,
            decoder: CompositeDecoder,
            data: Map<Any, Any>
        ) {
            this.data = data
            this.descriptor = descriptor
            this.json = json
            this.decoder = decoder
        }

        open fun setKey(key: String, valueIndex: Int) {
            valueSerializer = null
            decodedValue = null
            jsonElement = null
            this.keyString = key
            this.valueIndex = valueIndex
        }

        @OptIn(ExperimentalSerializationApi::class)
        protected open fun decodeJsonElement(): JsonElement {
            return decoder!!.decodeSerializableElement(descriptor, valueIndex, JsonElement.serializer())
        }

        @OptIn(ExperimentalSerializationApi::class)
        final override fun <T: Any> decodeValue(serializer: KSerializer<T>): T? {
            if (valueSerializer == null) {
                valueSerializer = serializer.unsafeCast()
                decodedValue = when (val jsonValue = jsonElement) {
                    null -> decoder!!.decodeNullableSerializableElement(descriptor, valueIndex, serializer)
                    else -> when (jsonValue) {
                        JsonNull -> null
                        else -> json.decodeFromJsonElement(serializer, jsonValue)
                    }
                }
            }

            return decodedValue.unsafeCast()
        }

        open fun delayKey() {
            val jsonValue = getJsonValue()
            if (jsonValue == JsonNull) return
            delayedKeys[keyString] = jsonValue
            delayedKeysCapacity = maxOf(delayedKeysCapacity, delayedKeys.size)
        }

        fun releaseDecoder() { decoder = null }

        open fun startDelayedKeyIteration() {
            // allocates map iterator
            delayedKeysIterator = delayedKeys.iterator()
        }

        open fun setNextDelayedKey() {
            val i = delayedKeysIterator ?: error("iteration not started or reached the end of delayed keys")
            val entry = i.next()
            setKey(entry.key, 0)
            jsonElement = entry.value
        }

        open fun hasMoreDelayedKeys(): Boolean {
            val i = delayedKeysIterator ?: return false
            if (i.hasNext()) return true
            delayedKeysIterator = null
            return false
        }

        open fun hasDelayedKeys(): Boolean = delayedKeys.isNotEmpty()
    }

    protected open fun acquireContext(
        descriptor: SerialDescriptor,
        json: Json,
        decoder: CompositeDecoder,
        data: Map<Any, Any>
    ): ResolveContextImpl = contextPool.get().acquire().also {
        it.prepare(descriptor, json, decoder, data)
    }

    protected open fun releaseContext(context: ResolveContextImpl) {
        contextPool.get().release(context)
    }

    @OptIn(ExperimentalSerializationApi::class)
    final override fun deserialize(decoder: JsonDecoder): T = decoder.decodeStructure(descriptor) {
        val capacity = decodeCollectionSize(descriptor).let { if (it >= 0) it else 8 }
        val json = decoder.json
        val data = HashMap<Any, Any>(capacity)
        var allKeysDecoded = true
        val ctx = acquireContext(descriptor, json, this, data.unsafeCast())
        ctx.state = initContextState(json)

        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break

            val keyString = decodeStringElement(descriptor, index)
            val valueIndex = decodeElementIndex(descriptor)
            ctx.setKey(keyString, valueIndex)

            when(val result = resolve(ctx)) {
                is DynoKey<*> -> {
                    val value = ctx.decodeValue(result.serializer)
                    if (value != null) data[result] = value
                }
                ResolveResult.Skip -> ctx.skipValue()
                else -> {
                    val value = ctx.getJsonValue()
                    if (value != JsonNull) {
                        if (result is ResolveResult.Keep) {
                            allKeysDecoded = false
                            data[keyString] = value
                        } else {
                            ctx.delayKey()
                        }
                    }
                }
            }
        }
        ctx.releaseDecoder()
        if (ctx.hasDelayedKeys() && !decodeDelayedKeys(ctx, decoder, data)) allKeysDecoded = false
        // No need to defer. try-finally has a cost.
        // Nothing breaks if object will not return to the pool,
        // just more allocations on slow path
        val state = ctx.state
        releaseContext(ctx)
        createMap(state, data, json.takeUnless { allKeysDecoded })
    }

    private fun decodeDelayedKeys(
        ctx: ResolveContextImpl,
        decoder: JsonDecoder,
        data: HashMap<Any, Any>
    ): Boolean {
        var allKeysDecoded = true

        ctx.startDelayedKeyIteration()

        while (ctx.hasMoreDelayedKeys()) {
            ctx.setNextDelayedKey()

            val result = postResolve(ctx)
            if (result is DynoKey<*>) {
                data[result] = decoder.json.decodeFromJsonElement(result.serializer, ctx.getJsonValue())
            } else if (result !is ResolveResult.Skip) {
                data[ctx.keyString] = ctx.getJsonValue()
                allKeysDecoded = false
            }
        }

        return allKeysDecoded
    }

    final override fun createMap(data: MutableMap<Any, Any>?, json: Json?): T =
        createMap(null, data, json)

    /**
     * @param state see [ResolveContext.state]
     */
    protected abstract fun createMap(state: Any?, data: MutableMap<Any, Any>?, json: Json?): T

    private companion object {
        class ContextPool: AbstractObjectPool<ResolveContextImpl>(1..4) {
            override fun allocate() = ResolveContextImpl()
            override fun beforeRelease(value: ResolveContextImpl) { value.close() }
        }
        val contextPool = ThreadLocal(::ContextPool)
    }
}

/** Reified version of [ResolveContext.decodeValue]. */
inline fun <reified T: Any> BaseResolveContext.decodeValue(): T? =
    decodeValue(serializer<T>())