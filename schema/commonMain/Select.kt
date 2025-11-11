package dyno

import karamel.utils.unsafeCast

open class Selector<S: DynoSchema, R>(val ent: Entity<S>) {
    @PublishedApi internal var result: R? = null
    @PublishedApi internal var selected = false

    inline fun <reified T: S> on(body: T.(Entity<T>) -> R) {
        if (selected) return
        val s = ent.schema
        if (s is T) {
            result = s.body(ent.unsafeCast<Entity<T>>())
            selected = true
        }
    }
}

class SingleSelector<S: DynoSchema, R: Any>(ent: Entity<S>): Selector<S, R>(ent) {
    inline fun orElse(body: S.(Entity<S>) -> R) {
        if (selected) return
        result = ent.schema.body(ent)
        selected = true
    }
}

inline fun <S: DynoSchema, R> Entity<S>.select(body: Selector<S, R>.() -> Unit): R? =
    Selector<S, R>(this).apply(body).result

inline fun <S: DynoSchema, R: Any> Entity<S>.selectSingle(body: SingleSelector<S, R>.() -> Unit): R =
    SingleSelector<S, R>(this).apply(body).result ?: error("orElse {} is missing")