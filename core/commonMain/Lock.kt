package dyno

internal expect class Lock {
    constructor()
    fun lock()
    fun unlock()
}