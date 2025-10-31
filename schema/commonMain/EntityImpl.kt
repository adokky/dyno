package dyno

import karamel.utils.unsafeCast
import kotlinx.serialization.json.Json

@PublishedApi
internal class EntityImpl<S: TypedDynoSchema<S>>: DynoMapImpl<SchemaProperty<S, *>>, MutableEntity<S> {
    constructor(): super()
    constructor(capacity: Int): super(capacity)
    constructor(entries: Collection<DynoEntry<SchemaProperty<S, *>, *>>): super(entries)
    constructor(other: Entity<S>): super(other)
    constructor(data: HashMap<Any, Any>?, json: Json?): super(data, json)

    override fun copy(): EntityImpl<S> = EntityImpl(this)

    override fun <T> get(key: SchemaProperty<S, T>): T? {
        return getUnsafe(key)
    }

    override fun <T> getOrFail(key: SchemaProperty<S, T>): T & Any {
        return getUnsafe(key) ?: throw NoSuchDynoKeyException(key)
    }

    override fun contains(key: SchemaProperty<S, *>): Boolean {
        return containsUnsafe(key)
    }

    override fun plus(entry: EnityEntry<S>): EntityImpl<S> {
        return copy().also { it += entry }
    }

    override fun minus(key: SchemaProperty<S, *>): EntityImpl<S> {
        return copy().also { it -= key }
    }

    override fun <T> set(key: SchemaProperty<S, in T>, value: T) {
        if (value == null) removeUnsafe(key) else setUnsafe(key.unsafeCast(), value)
    }

    override fun <T> put(key: SchemaProperty<S, T>, value: T): T? {
        return putUnsafe(key, value)
    }

    override fun <T> remove(key: SchemaProperty<S, T>): T? {
        return removeAndGetUnsafe(key)
    }

    override fun set(entry: EnityEntry<S>) {
        setUnsafe(entry)
    }

    override fun <T> put(entry: DynoEntry<SchemaProperty<S, T & Any>, T>): T? {
        return putUnsafe(entry)
    }

    override fun putAll(entries: Iterable<EnityEntry<S>>) {
        for (entry in entries) {
            setUnsafe(entry)
        }
    }

    override fun plusAssign(entry: EnityEntry<S>) {
        putUnsafe(entry)
    }

    override fun minusAssign(key: SchemaProperty<S, *>) {
        removeUnsafe(key)
    }
}