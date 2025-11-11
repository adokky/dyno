package dyno

sealed class Vehicle(name: String): EntitySchema(name) {
    val name by dynoKey<String>()

    companion object: Vehicle("vehicle"), Polymorphic
}

object Bicycle: Vehicle("bicycle") {
    val electric by dynoKey<Boolean>()
}

object Car: Vehicle("car") {
    val wheels by dynoKey<Int?>()
}