package dyno

internal object EmptyDynamicObject: DynamicObjectImpl() {
    override fun <T> set(key: DynoKey<T>, value: T?) = error()
    override fun set(entry: DynoEntry<DynoKey<*>, *>) = error()
    override fun <T> put(key: DynoKey<T>, value: T & Any): T = error()
    override fun <T> put(entry: DynoEntry<DynoKey<T>, T>): T = error()
    override fun <T> remove(key: DynoKey<out T>): T = error()
    override fun putAll(entries: Iterable<DynoEntry<DynoKey<*>, *>>) = error()
    override fun plusAssign(entry: DynoEntry<DynoKey<*>, *>) = error()
    override fun minusAssign(key: DynoKey<*>) = error()

    private fun error(): Nothing = error("this DynamicObject is immutable")
}