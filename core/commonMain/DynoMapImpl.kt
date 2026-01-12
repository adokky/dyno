package dev.dokky.dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

// Internal methods with the "Unsafe" suffix are used as prototypes
// for public methods but allow passing any DynoKey<*>
@InternalDynoApi
abstract class DynoMapImpl(
    data: MutableMap<Any, Any>?,
    json: Json?,
    readSafety: DynoReadSafety
): MutableDynoMapBase {
    // see Unsafe.data
    private var data: HashMap<Any, Any>? = data?.toHashMap()
    // see Unsafe.json
    private var json: Json? = json.takeIf { data != null }

    private val readMode: Int

    private val actualReadMode: Int get() = readMode shr 8
    private val proposedReadMode: Int get() = readMode and 0xff

    val readSafety: DynoReadSafety get() = DynoReadSafety.entries[proposedReadMode]

    init {
        val proposedReadMode = readSafety.ordinal
        val actualReadMode = when {
            data == null || json == null -> 0
            else -> proposedReadMode
        }
        readMode = actualReadMode.shl(8).or(proposedReadMode)
    }

    /**
     * Cached hash code. Updated on every mutation.
     * Zero used as "uninitialized" marker.
     * Hash code that is actually zero will be recalculated on every [hashCode] invocation.
     */
    private var _hashCode = 0

    override fun clear() {
        data?.clear()
        json = null
        _hashCode = 0
    }

    constructor(): this(null, null, DynoReadSafety.SYNCHRONIZED)

    constructor(capacity: Int): this(HashMap(capacity), null, DynoReadSafety.SYNCHRONIZED)

    constructor(entries: Collection<DynoEntry<*, *>>): this(
        if (entries.isEmpty()) null else createData(entries),
        json = null,
        DynoReadSafety.SYNCHRONIZED
    )

    constructor(other: DynoMapImpl, readSafety: DynoReadSafety): this(
        data = other.syncIfEnabled { other.data?.let(::HashMap) },
        json = other.json,
        readSafety = readSafety
    )

    constructor(other: DynoMapBase, readSafety: DynoReadSafety? = null): this(
        other = other as DynoMapImpl,
        readSafety = readSafety ?: other.readSafety
    )

    abstract override fun copy(): DynoMapImpl

    /**
     * Each entry can be one of these types:
     * 1. encoded: `String -> JsonElement`
     * 2. decoded: `DynoKey -> Any?`
     */
    @Suppress("UnusedReceiverParameter")
    val DynoMapBase.Unsafe.data: MutableMap<Any, Any>? get() = this@DynoMapImpl.data

    /**
     * If this property is not null, it means that this [DynoMapImpl] was deserialized,
     * and [data] may contain [String] keys with [JsonElement] values.
     * Upon the first attempt to read a value by such a key,
     * the value is decoded and written under the corresponding [DynoKey],
     * and the old [String] key is removed (exceptions: [getStateless] and [DynoReadSafety.NO_CACHE]).
     */
    @Suppress("UnusedReceiverParameter")
    val DynoMapBase.Unsafe.json: Json? get() = this@DynoMapImpl.json

    final override val size: Int get() {
        val data = data ?: return 0
        return syncIfEnabled { data.size }
    }

    final override fun <T> DynoMapBase.Unsafe.get(key: DynoKey<T>): T? {
        val data = this@DynoMapImpl.data ?: return null
        val json = this@DynoMapImpl.json

        return if (actualReadMode == DynoReadSafety.SYNCHRONIZED.ordinal) {
            getThreadSafe(key, json!!, data)
        } else {
            getUnsafe(key, json, data, store = actualReadMode != DynoReadSafety.NO_CACHE.ordinal)
        }
    }

    /**
     * Unlike [get], does not put deserialized value in [DynoMapImpl.data].
     */
    final override fun <T> DynoMapBase.Unsafe.getStateless(key: DynoKey<T>): T? {
        val data = this@DynoMapImpl.data ?: return null
        val json = this@DynoMapImpl.json

        return getUnsafe(key, json, data, store = false)
    }

    private fun <T> getUnsafe(
        key: DynoKey<T>,
        json: Json?,
        data: HashMap<Any, Any>,
        store: Boolean
    ): T? {
        val v = json?.let { if (store) data.remove(key.name) else data[key.name] }
            ?: return data[key].unsafeCast()

        return json.decodeValue(key, v.unsafeCast()).also { decoded ->
            if (store && decoded != null) {
                data[key] = decoded
            }
        }
    }

    private fun <T> getThreadSafe(key: DynoKey<T>, json: Json, data: HashMap<Any, Any>): T? {
        sync {
            data.remove(key.name)?.let { v ->
                val decoded = json.decodeValue(key, v.unsafeCast())
                if (decoded != null) data[key] = decoded
                return decoded
            }
            return data[key].unsafeCast()
        }
    }

    final override fun <T> DynoMapBase.Unsafe.set(key: DynoKey<T>, value: T & Any) {
        key.onAssign?.apply { key.process(value) }
        val d = data
        if (d == null) {
            getOrInitData()[key] = value
            updateHashKeyAdded(key.name)
        } else {
            if (d.remove(key.name) == null) updateHashKeyAdded(key.name)
            d[key] = value
        }
    }

    final override fun <T> DynoMapBase.Unsafe.put(key: DynoKey<T>, value: T?): T? {
        if (value == null) return removeAndGet(key)

        key.onAssign?.apply { key.process(value) }

        val data = getOrInitData()
        val old: T?
        val oldEncoded = data.remove(key.name)
        if (oldEncoded == null) {
            old = data.put(key, value).unsafeCast()
            if (old == null) updateHashKeyAdded(key.name)
        } else {
            old = json!!.decodeValue(key, oldEncoded.unsafeCast())
            data[key] = value
        }

        return old
    }

    final override fun <T> DynoMapBase.Unsafe.removeAndGet(key: DynoKey<T>): T? {
        val data = data ?: return null

        var res = data.remove(key)
        if (res == null) {
            res = data.remove(key.name)?.let { v ->
                json!!.decodeValue(key, v as JsonElement)
            }
        }

        if (res != null) updateHashKeyRemoved(key.name)

        return res.unsafeCast()
    }

    final override fun <T> DynoMapBase.Unsafe.put(entry: DynoEntry<*, T>): T? =
        put(entry.key, entry.value)

    final override fun DynoMapBase.Unsafe.set(entry: DynoEntry<*, *>) {
        set(entry.key.unsafeCast(), entry.value)
    }

    /** @return `true` if the [key] has been successfully removed; `false` if it was not contained in the map */
    final override fun DynoMapBase.Unsafe.remove(key: DynoKey<*>): Boolean {
        val data = data ?: return false
        val removed = (data.remove(key) ?: data.remove(key.name)) != null
        if (removed) updateHashKeyRemoved(key.name)
        return removed
    }

    /** @return `true` if the [key] has been successfully removed; `false` if it was not contained in the map */
    final override fun DynoMapBase.Unsafe.remove(key: String): Boolean {
        val data = data ?: return false
        val removed = (data.remove(key) ?: data.remove(DynoKey(key, Unit.serializer()))) != null
        if (removed) updateHashKeyRemoved(key)
        return removed
    }

    final override fun DynoMapBase.Unsafe.contains(key: DynoKey<*>): Boolean {
        val data = data ?: return false
        return syncIfEnabled { key.name in data || key in data }
    }

    override fun contains(key: String): Boolean {
        val data = data ?: return false
        return syncIfEnabled { key in data || SimpleDynoKey<Unit>(key) in data }
    }

    internal fun <T> Json.decodeValue(key: DynoKey<T>, v: JsonElement): T? = when {
        v === JsonNull -> null
        else -> decodeFromJsonElement(key.serializer, v).also { value ->
            key.onDecode?.apply { key.process(value) }
        }
    }

    internal fun getOrInitData(): HashMap<Any, Any> =
        data ?: (HashMap<Any, Any>(2).also { this.data = it })

    val keyNames: Sequence<String> get() {
        val data = data ?: return emptySequence()

        val entries = when {
            actualReadMode == 0 -> data.entries
            else -> sync { data.entries.toList() }
        }

        return entries.asSequence().map { it.key as? String ?: (it.key as DynoKey<*>).name }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DynoMapImpl) return false

        return when(actualReadMode or other.actualReadMode < DynoReadSafety.SYNCHRONIZED.ordinal) {
            true -> equalsOther(other)
            else -> sync(this, other) { equalsOther(other) }
        }
    }

    private fun equalsOther(other: DynoMapImpl): Boolean {
        if (other._hashCode != 0 &&
            _hashCode != 0 &&
            other._hashCode != _hashCode)
            return false

        // heuristic to avoid linear probes
        val o1: DynoMapImpl
        val o2: DynoMapImpl
        if (this.json != null && other.json == null) {
            o1 = other
            o2 = this
        } else {
            o1 = this
            o2 = other
        }

        val data1 = o1.data
        val data2 = o2.data

        if (data1 == null) {
            return data2 == null || data2.isEmpty()
        } else {
            if (data2 == null) return data1.isEmpty()
            if (data1.size != data2.size) return false
        }

        for ((k, v) in data1) {
            if (k is DynoKey<*>) {
                val store = o2.actualReadMode != DynoReadSafety.NO_CACHE.ordinal
                if (o2.getUnsafe(k, o2.json, data2, store) != v) return false
            } else {
                k as String
                val otherJsonV = data2[k]
                if (otherJsonV != null) {
                    if (otherJsonV != v) return false
                } else {
                    val (key, otherV) = data2.entries
                        .find { (it.key as? DynoKey<*>)?.name == k }
                        ?: return false
                    val thisV = json!!.decodeValue(key as DynoKey<*>, v as JsonElement)
                    if (thisV != otherV) return false
                }
            }
        }

        return true
    }

    private fun incHashCode(key: String, mult: Int) { _hashCode += key.hashCode() * mult }
    private fun updateHashKeyAdded(key: String)   { if (_hashCode != 0) incHashCode(key,  HASH_CODE_MULT) }
    private fun updateHashKeyRemoved(key: String) { if (_hashCode != 0) incHashCode(key, -HASH_CODE_MULT) }

    override fun hashCode(): Int {
        val d = data ?: return 0
        return syncIfEnabled { hashCode(d) }
    }

    override fun toString(): String {
        val data = data ?: return "{}"
        return syncIfEnabled { toString(data) }
    }

    private fun hashCode(data: HashMap<Any, Any>): Int {
        if (_hashCode != 0) return _hashCode

        if (data.isEmpty()) return 0

        for ((k, _) in data) {
            val keyName = k as? String ?: (k as DynoKey<*>).name
            incHashCode(keyName, HASH_CODE_MULT)
        }

        return _hashCode
    }

    private fun toString(data: HashMap<Any, Any>): String {
        if (data.isEmpty()) return "{}"

        return buildString {
            append('{')
            for ((k, v) in data) {
                val pid = if (k is DynoKey<*>) k.name else k
                append(pid.toString())
                append('=')
                if (v is String) append('"')
                append(v)
                if (v is String) append('"')
                append(',')
            }
            setLength(length - 1)
            append('}')
        }
    }

    private inline fun <R> syncIfEnabled(body: () -> R): R {
        if (actualReadMode == 0) return body()
        return sync { body() }
    }

    internal companion object {
        const val HASH_CODE_MULT = 31

        private fun createData(entries: Collection<DynoEntry<*, *>>): HashMap<Any, Any> =
            HashMap<Any, Any>(entries.size.coerceAtLeast(2), 1f).apply {
                for (arg in entries) {
                    val value = arg.value
                    val key = arg.key.unsafeCast<DynoKey<Any>>()
                    key.onAssign?.apply { key.process(value) }
                    put(key, value)
                }
            }

        private fun MutableMap<Any, Any>.toHashMap() = this as? HashMap ?: HashMap(this)
    }
}

internal val DynoMapBase.readSafety: DynoReadSafety get() = this.unsafeCast<DynoMapImpl>().readSafety