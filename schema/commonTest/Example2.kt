package dyno

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

private open class Person: SimpleDynoSchema("fsfds") {
    val age by dynoKey<Int>()
}

private object Employee: Person() {
    override fun name(): String = "employee"
    val department by dynoKey<String>()
}

private typealias PersonMap = @Serializable(Person::class) DynoMap<*>

class Example2 {
    @Test
    fun test() {
        val obj = Employee.new {
            age set 23
            department set "x"
        } as MutableDynoMap<DynoKey<*>>

        assertEquals(23, obj[Employee.age])
        assertEquals("x", obj[Employee.department])

        obj[Employee.age] = 45
        obj[Employee.department] = "y"

        assertEquals(45, obj[Employee.age])
        assertEquals("y", obj.put(Employee.department, "z"))
    }
}

