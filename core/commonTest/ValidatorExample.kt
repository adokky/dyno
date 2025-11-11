package dyno

object Person {
    val age = DynoKey<Int>("age").validate {
        require(it < 0) { "'age' must be positive" }
    }
    val name by dynoKey<String?>().notBlank()
    val name2 by dynoKey<String>().notBlank()
}

fun <R: DynoKeySpec<String>> R.notBlank() = validate {
    require(it.isNotBlank()) { "property '$name' must not be empty" }
}

