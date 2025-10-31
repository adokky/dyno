package dyno

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
    json: Json?
): MutableDynoMapBase {
    /**
     * Each entry can be one of these types:
     * 1. encoded: `String -> JsonElement`
     * 2. decoded: `DynoKey -> Any?`
     */
    internal var data: HashMap<Any, Any>? = data?.toHashMap()
        private set

    /**
     * If this property is not null, it means that this [DynoMapImpl] was deserialized,
     * and [data] may contain [String] keys with [JsonElement] values.
     * Upon the first attempt to read a value by such a key,
     * the value is decoded and written under the [DynoKey],
     * and the old [String] key is removed.
     */
    internal var json: Json? = json.takeIf { data != null }
        private set

    /**
     * Cached hash code. Updated on every mutation.
     * Zero used as "uninitialized" marker.
     * Hash code tha actually zero will be recalculated on every [hashCode] invocation.
     */
    private var _hashCode = 0

    constructor(): this(null, null)

    constructor(capacity: Int): this(HashMap(capacity), null)

    constructor(entries: Collection<DynoEntry<*, *>>): this(
        if (entries.isEmpty()) null else createData(entries),
        json = null
    )

    constructor(other: DynoMapImpl): this(
        data = other.data?.let(::HashMap),
        json = other.json
    )

    constructor(other: DynoMapBase): this(other as DynoMapImpl)

    abstract override fun copy(): DynoMapImpl

    final override val size: Int get() = data?.size ?: 0

    override fun clear() {
        data?.clear()
        json = null
        _hashCode = 0
    }

    override fun <T: Any> DynoMapBase.Unsafe.get(key: DynoKey<T>): T? {
        val data = data ?: return null
        val json = json ?: return data[key].unsafeCast()

        val v = data.remove(key.name)
            ?: data[key]
            ?: return null

        return decodeAndPutValue(key.unsafeCast(), v, json)
    }

    /**
     * Unlike [get], does not put deserialized value in [DynoMapImpl.data].
     */
    override fun <T: Any> DynoMapBase.Unsafe.getStateless(key: DynoKey<T>): T? {
        val data = data ?: return null
        val json = json ?: return data[key].unsafeCast()

        val v = data[key.name]
            ?: data[key]
            ?: return null

        return when (v) {
            !is JsonElement -> v.unsafeCast()
            else -> json.decodeValue(key, v)
        }
    }

    override fun <T: Any> DynoMapBase.Unsafe.set(key: DynoKey<T>, value: T) {
        val d = data
        if (d == null) {
            getOrInitData()[key] = key.onAssign(value)
            updateHashKeyAdded(key.name)
        } else {
            if (d.remove(key.name) == null) updateHashKeyAdded(key.name)
            d[key] = value
        }
    }

    override fun <T: Any> DynoMapBase.Unsafe.put(key: DynoKey<T>, value: T?): T? {
        if (value == null) return removeAndGet(key)

        val data = getOrInitData()

        val value = key.onAssign(value)
        val old: T?
        val oldEncoded = data.remove(key.name)
        if (oldEncoded != null) {
            old = json!!.decodeValue(key, oldEncoded.unsafeCast())
            data[key] = value
        } else {
            old = data.put(key, value).unsafeCast()
            if (old == null) updateHashKeyAdded(key.name)
        }

        return old
    }

    override fun <T: Any> DynoMapBase.Unsafe.removeAndGet(key: DynoKey<T>): T? {
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

    override fun <T: Any> DynoMapBase.Unsafe.put(entry: DynoEntry<*, T?>): T? =
        put(entry.key, entry.value)

    override fun DynoMapBase.Unsafe.set(entry: DynoEntry<*, *>) {
        when (val v = entry.value) {
            null -> remove(entry.key)
            else -> set(entry.key.unsafeCast(), v)
        }
    }

    /** @return `true` if the [key] has been successfully removed; `false` if it was not contained in the map */
    override fun DynoMapBase.Unsafe.remove(key: DynoKey<*>): Boolean {
        val data = data ?: return false
        val removed = (data.remove(key) ?: data.remove(key.name)) != null
        if (removed) updateHashKeyRemoved(key.name)
        return removed
    }

    /** @return `true` if the [key] has been successfully removed; `false` if it was not contained in the map */
    override fun DynoMapBase.Unsafe.remove(key: String): Boolean {
        val data = data ?: return false
        val removed = (data.remove(key) ?: data.remove(DynoKey(key, Unit.serializer()))) != null
        if (removed) updateHashKeyRemoved(key)
        return removed
    }

    override fun DynoMapBase.Unsafe.contains(key: DynoKey<*>): Boolean {
        val data = data ?: return false
        return key.name in data || key in data
    }

    override fun contains(key: String): Boolean {
        val data = data ?: return false
        return key in data || SimpleDynoKey<Unit>(key) in data
    }

    private fun <T: Any> decodeAndPutValue(key: DynoKey<T>, v: Any, json: Json): T? = when (v) {
        is JsonElement -> json.decodeValue(key, v)?.also { getOrInitData()[key] = it }
        else -> v.unsafeCast()
    }

    private fun <T: Any> Json.decodeValue(key: DynoKey<T>, v: JsonElement): T? = when {
        v === JsonNull -> null
        else -> key.onDecode(decodeFromJsonElement(key.serializer, v))
    }

    private fun getOrInitData(): HashMap<Any, Any> =
        data ?: (HashMap<Any, Any>(2).also { this.data = it })

    val keyNames: Sequence<String> get() {
        return (data ?: return emptySequence())
            .entries.asSequence()
            .map { it.key as? String ?: (it.key as DynoKey<*>).name }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DynoMapImpl) return false
        if (this.size != other.size) return false
        if (other._hashCode != 0 && _hashCode != 0 && other._hashCode != _hashCode) return false

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

        // size check completely eliminates all null cases
        val data1 = o1.data ?: return true
        val data2 = o2.data ?: return true

        for ((k, v) in data1) {
            if (k is DynoKey<*>) {
                if (with(o2) { DynoMapBase.Unsafe.get(k) } != v) return false
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
        if (_hashCode != 0) return _hashCode

        val d = data ?: return 0
        if (d.isEmpty()) return 0

        for ((k, _) in d) {
            val keyName = k as? String ?: (k as DynoKey<*>).name
            incHashCode(keyName, HASH_CODE_MULT)
        }

        return _hashCode
    }

    override fun toString(): String {
        val data = data

        if (data.isNullOrEmpty()) return "{}"

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

    internal companion object {
        private const val HASH_CODE_MULT = 31

        private fun createData(entries: Collection<DynoEntry<*, *>>): HashMap<Any, Any> =
            HashMap<Any, Any>(entries.size.coerceAtLeast(2), 1f).apply {
                for (arg in entries) {
                    if (arg.value != null) {
                        put(arg.key.unsafeCast(), arg.value!!)
                    }
                }
            }

        private fun MutableMap<Any, Any>.toHashMap() = this as? HashMap ?: HashMap(this)
    }
}