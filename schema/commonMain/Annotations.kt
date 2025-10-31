@file:JvmName("SchemaAnnotationsKt")

package dyno

import kotlinx.serialization.MetaSerializable
import kotlin.jvm.JvmName

@MetaSerializable
annotation class Schema(val value: String)