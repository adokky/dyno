package dyno

import karamel.utils.unsafeCast

/**
 * Executes a selection logic over the schema of this [Entity], allowing to define multiple conditional branches
 * based on the actual schema type and providing a fallback via [Selector.orElse].
 *
 * This function is useful when you need to extract a value of type [R] based on the runtime type of the schema,
 * similar to a `when` expression but with type-safe branches. Each branch is defined using [Selector.on],
 * which is only executed if the schema is of the specified type.
 *
 * If no [Selector.on] branch matches and no [Selector.orElse] is defined, an exception is thrown.
 *
 * - It is an error to call [Selector.orElse] more than once.
 * - It is an error to not match any branch and not define [Selector.orElse].
 *
 * Example:
 * ```
 * val result: String = entity.select> {
 *     on<AdminSchema> { ent ->
 *         "Admin: ${ent[name]}"
 *     }
 *     on<UserSchema> { ent ->
 *         "User: ${ent[username]}"
 *     }
 *     orElse { ent ->
 *         "Unknown: ${ent.schema.name()}"
 *     }
 * }
 * ```
 *
 * @throws IllegalStateException if [Selector.orElse] is called more than once.
 * @throws IllegalStateException if no match is found and [Selector.orElse] is not defined.
 */
inline fun <S: DynoSchema, R: Any> Entity<S>.select(body: Selector<S, R>.() -> Unit): R =
    Selector<S, R>(this).apply(body).getResult()


class Selector<S: DynoSchema, R>
@PublishedApi internal constructor(val ent: Entity<S>)
{
    @PublishedApi internal var result: R? = null
    @PublishedApi internal var selected: Boolean = false

    inline fun <reified T: S> on(body: T.(Entity<T>) -> R) {
        val s = ent.schema
        if (s is T) {
            result = s.body(ent.unsafeCast<Entity<T>>())
            selected = true
        }
    }

    inline fun orElse(body: S.(Entity<S>) -> R) {
        if (selected) error("'orElse' called twice")
        result = ent.schema.body(ent)
        selected = true
    }

    @PublishedApi
    internal fun getResult(): R {
        if (!selected) throwResultIsNotSet()
        return result.unsafeCast()
    }

    private fun throwResultIsNotSet(): Nothing =
        error("no matches found for schema '${ent.schema.name()}' and no 'orElse' defined")
}