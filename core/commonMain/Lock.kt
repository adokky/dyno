package dyno

internal expect class Lock {
    constructor()
    fun lock()
    fun unlock()
}

expect inline fun <R> DynoMapImpl.sync(body: () -> R): R

expect inline fun <R> sync(lock1: Any, lock2: Any, body: () -> R): R
