package dyno

// no-op lock
internal actual class Lock {
    actual constructor()
    actual fun lock() {}
    actual fun unlock() {}
}