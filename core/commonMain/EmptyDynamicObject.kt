package dyno

internal object EmptyDynamicObject: DynamicObjectImpl() {
    override fun <T : Any> DynoMapBase.Unsafe.set(key: DynoKey<T>, value: T) = error()
    override fun DynoMapBase.Unsafe.set(entry: DynoEntry<*, *>) = error()
    override fun <T : Any> DynoMapBase.Unsafe.put(key: DynoKey<T>, value: T?) = error()
    override fun <T : Any> DynoMapBase.Unsafe.put(entry: DynoEntry<*, T?>) = error()
    override fun DynoMapBase.Unsafe.remove(key: DynoKey<*>) = error()
    override fun DynoMapBase.Unsafe.remove(key: String) = error()
    override fun <T : Any> DynoMapBase.Unsafe.removeAndGet(key: DynoKey<T>) = error()

    private fun error(): Nothing = error("this DynamicObject is immutable")
}