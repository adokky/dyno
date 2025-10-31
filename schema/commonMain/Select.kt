package dyno

import karamel.utils.unsafeCast

sealed class S { object A1: S(); object A2: S() }

class GenericInferenceTest<T: Any>(val schema: T)

fun GenericInferenceTest<S.A1>.foo1(): String = TODO()
fun GenericInferenceTest<S.A2>.foo2(): String = TODO()

open class Selector<S: Any, R>(val ent: GenericInferenceTest<S>) {
    @PublishedApi internal var result: R? = null
    @PublishedApi internal var selected = false

    inline fun <reified T: S> on(body: GenericInferenceTest<T>.() -> R) {
        if (selected) return
        if (ent.schema is T) {
            result = ent.unsafeCast<GenericInferenceTest<T>>().body()
            selected = true
        }
    }
}

class SingleSelector<S: Any, R: Any>(ent: GenericInferenceTest<S>): Selector<S, R>(ent) {
    inline fun orElse(body: GenericInferenceTest<S>.() -> R) {
        if (selected) return
        result = ent.body()
        selected = true
    }
}

inline fun <S: Any, R> GenericInferenceTest<S>.select(body: Selector<S, R>.() -> Unit): R? =
    Selector<S, R>(this).apply(body).result

inline fun <S: Any, R: Any> GenericInferenceTest<S>.selectSingle(body: SingleSelector<S, R>.() -> Unit): R =
    SingleSelector<S, R>(this).apply(body).result ?: error("orElse {} is missing")

private fun test1(ent: GenericInferenceTest<S>) {
    val result: List<Any> = ent.selectSingle {
        on<S.A1> { listOf(1, 2, 3) }
        on<S.A2> { listOf("1", "2", "3") }
        orElse { listOf(true, false) }
    }
}

private fun test2(ent: GenericInferenceTest<S>) {
//    fun <E: GenericInferenceTest<S>, S: Any, R> E.switch(body: E.(schema: S) -> R): R = body(schema)
//    ent.switch { s ->
//        when(s) {
//            S.A1 -> foo1()
//            S.A2 -> foo2()
//        }
//    }
}

private fun test3(ent: GenericInferenceTest<S>) {
//    when(ent.schema) {
//        S.A1 -> ent.foo1()
//        S.A2 -> ent.foo2()
//    }
}