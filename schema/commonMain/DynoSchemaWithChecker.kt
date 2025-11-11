package dyno

internal class DynoSchemaWithChecker(schema: DynoSchema): DynoSchema by schema {
    val checker = EntityConsistensyChecker(schema)
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