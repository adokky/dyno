package dyno

/**
 * Returns a new [DynoSchema] that includes all properties from both the original schema and the [other] schema.
 * If there are conflicting property names between the two schemas, an exception is thrown.
 *
 * The resulting schema name is copied from [other] schema.
 *
 * @throws IllegalArgumentException if there is a property name conflict.
 */
fun DynoSchema.combine(other: DynoSchema): DynoSchema {
    val newSchema = SimpleDynoSchema(this)

    for (key in other.keys()) {
        require(newSchema.tryRegister(key)) {
            val clarification = when(val extName = other.name()) {
                name() -> "Extending schema '${name()}'"
                else -> "Combining schema '${name()}' with '$extName'"
            }
            "$clarification. Property conflict: '${key.name}'"
        }
    }

    return newSchema
}