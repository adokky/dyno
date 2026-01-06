package dyno

actual inline fun <R> DynoMapImpl.sync(body: () -> R): R {
    return synchronized(this, body)
}

actual inline fun <R> sync(lock1: Any, lock2: Any, body: () -> R): R {
    val first: Any
    val second: Any

    if (lock1 === lock2) return synchronized(lock1, body)

    if (System.identityHashCode(lock1) < System.identityHashCode(lock2)) {
        first = lock1
        second = lock2
    } else {
        first = lock2
        second = lock1
    }

    return synchronized(first) {
        synchronized(second, body)
    }
}

