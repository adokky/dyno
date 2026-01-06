package dyno

/**
 * Enum representing different levels of read thread-safety for [DynoMap].
 *
 * Note: The entire [DynoMap] is not thread-safe for mutations from multiple threads.
 * These options only affect how reads are handled.
 */
enum class DynoReadSafety {
    /**
     * Unsafe read access - no synchronization.
     * Up to 2 times faster than [SYNCHRONIZED].
     *
     * WARNING: Only safe when:
     * - deserialization and all subsequent reads happen on the same thread,
     * - OR using [AbstractEagerDynoSerializer] without [AbstractEagerDynoSerializer.ResolveResult.Keep].
     */
    UNSAFE, // must be first!

    /**
     * Read operations are completely thread-safe, but with no caching of deserialized values.
     * Every read operation will re-decode the value.
     *
     * This ensures thread-safety without the overhead of synchronization,
     * but at the cost of performance on repeated reads.
     */
    NO_CACHE,

    /**
     * Default option - provides fully thread-safe read operations with caching.
     * Synchronizes access to deserialized values, ensuring safe concurrent reads.
     */
    SYNCHRONIZED; // must be last!
}