package dev.dokky.dyno

expect inline fun <R> DynoMapImpl.sync(body: () -> R): R

expect inline fun <R> sync(lock1: Any, lock2: Any, body: () -> R): R
