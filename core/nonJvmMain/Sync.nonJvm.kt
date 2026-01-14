package dev.dokky.dyno

actual internal inline fun <R> DynoMapImpl.sync(body: () -> R): R {
    return body()
}

actual internal inline fun <R> sync(lock1: Any, lock2: Any, body: () -> R): R {
    return body()
}