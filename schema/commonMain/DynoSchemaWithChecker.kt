package dyno

internal class DynoSchemaWithChecker(internal val original: DynoSchema): DynoSchema by original {
    val checker = EntityConsistensyChecker(original)
}

internal fun DynoSchema.checker(): EntityConsistensyChecker = when (this) {
    is AbstractDynoSchema<*> -> checker
    is DynoSchemaWithChecker -> checker
    else -> throwNoConsistencyChecker(this)
}

internal fun throwNoConsistencyChecker(schema: DynoSchema): Nothing {
    error("no EntityConsistensyChecker avalable for '${schema::class}'")
}

internal fun DynoSchema.withChecker(): DynoSchema = when (this) {
    is AbstractDynoSchema<*>, is DynoSchemaWithChecker -> this
    else -> DynoSchemaWithChecker(this)
}

internal fun DynoSchema.unwrap(): DynoSchema = if (this is DynoSchemaWithChecker) original else this