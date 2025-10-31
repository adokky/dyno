package dyno

import kotlinx.serialization.SerializationException

/**
 * Base interface for the exceptions that Thrown when attempting to access an unknown key
 * via [DynamicObject.getOrFail] / [DynoMap.getOrFail][getOrFail] or
 * during [DynamicObject] decoding when using eager deserialization strategy ([AbstractEagerDynoSerializer]).
 *
 * The [schema] property may be available when the exception is thrown during
 * eager deserialization.
 *
 * @property key The name of the unknown key that was accessed.
 * @property schema The schema name where the key was not found, available during eager deserialization.
 *
 * @see NoSuchDynoKeyException
 * @see NoSuchDynoKeySerializationException
 */
sealed interface NoSuchDynoKeyBaseException {
    val key: String
    val schema: String?
}

/**
 * Thrown when attempting to access an unknown key via [DynamicObject.getOrFail] / [DynoMap.getOrFail][getOrFail].
 *
 * @property schema The schema name where the key was not found, may be not available for this exception.
 */
class NoSuchDynoKeyException(
    override val key: String,
    override val schema: String? = null
): NoSuchElementException(buildMessage(key, schema)), NoSuchDynoKeyBaseException {
    constructor(key: DynoKey<*>, schema: String? = null): this(key.name, schema)
}

private fun buildMessage(key: String, schema: String?): String = buildString(64) {
    append("unknown property '")
    append(key)
    append('\'')

    if (schema != null) {
        append(" of schema '")
        append(schema)
        append('\'')
    }
}

/**
 * Thrown during [DynamicObject] decoding when using eager deserialization strategy ([AbstractEagerDynoSerializer]).
 *
 * @property schema The schema name where the key was not found.
 */
class NoSuchDynoKeySerializationException(override val key: String, override val schema: String):
    SerializationException(buildMessage(key, schema)), NoSuchDynoKeyBaseException
{
    constructor(key: DynoKey<*>, schema: String): this(key.name, schema)
}