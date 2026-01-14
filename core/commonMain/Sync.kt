package dev.dokky.dyno

internal expect inline fun <R> DynoMapImpl.sync(body: () -> R): R

internal expect inline fun <R> sync(lock1: Any, lock2: Any, body: () -> R): R
