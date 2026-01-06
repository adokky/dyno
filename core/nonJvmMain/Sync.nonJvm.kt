package dyno

actual inline fun <R> DynoMapImpl.sync(body: () -> R): R {
    return body()
}

actual inline fun <R> sync(lock1: Any, lock2: Any, body: () -> R): R {
    return body()
}