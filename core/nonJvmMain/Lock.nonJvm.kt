package dyno

// no-op lock
internal actual class Lock {
    actual constructor()
    actual fun lock() {}
    actual fun unlock() {}
}

actual inline fun <R> DynoMapImpl.sync(body: () -> R): R {
    return body()
}

actual inline fun <R> sync(lock1: Any, lock2: Any, body: () -> R): R {
    return body()
}