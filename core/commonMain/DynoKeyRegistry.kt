package dyno

/**
 * Interface for classes that want to automatically register [DynoKey] instances upon class loading.
 *
 * When a class containing properties delegated by [DynoKeyPrototype] implements this interface,
 * the [Internal.register] method will be invoked for each constructed [DynoKey].
 *
 * Example usage:
 * ```
 * abstract class MyKeyRegistry: DynoKeyRegistry {
 *     val registeredKeys = ArrayList<DynoKey<*>>()
 *
 *     override val DynoKeyRegistry.Internal.keys: Collection<DynoKey<*>>
 *         get() = registeredKeys.keys
 *
 *     override fun DynoKeyRegistry.Internal.register(key: DynoKey<*>) {
 *         registeredKeys.add(key)
 *     }
 * }
 *
 * object MySchema : MyKeyRegistry() {
 *     val myKey by dynoKey<String>()
 * }
 * ```
 */
interface DynoKeyRegistry {
    /**
     * Collection of registered keys.
     */
    val Internal.keys: Collection<DynoKey<*>>

    /**
     * Registers a key during property initialization.
     */
    fun Internal.register(key: DynoKey<*>)

    /**
     * Nested object used to scope key registration and access.
     */
    object Internal
}
