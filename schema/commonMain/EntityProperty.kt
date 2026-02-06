package dev.dokky.dyno

import kotlin.properties.ReadOnlyProperty

/**
 * Similar to [SimpleDynoKey] but bound to specific [DynoSchema] (sub)type [S].
 */
interface EntityProperty<in S: DynoSchema, T> :
    DynoKey<T>,
    Comparable<DynoKey<T>>,
    ReadOnlyProperty<Any, EntityProperty<S, T>>,
    DynoKeySpec<T & Any>
{
    val DynoKeySpec.Internal.index: Int
}