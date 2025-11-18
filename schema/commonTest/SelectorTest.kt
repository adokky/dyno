package dyno

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SelectorTest {
    @Test
    fun test() {
        val car = Car.new {
            name set "CAR"
            wheels set 4
        }

        val bicycle = Bicycle.new {
            name set "CAR"
            electric set false
        }

        fun test(vehicle: Entity<Vehicle>): String = vehicle.selectSingle {
            on<Car> { it[wheels].toString() }
            on<Bicycle> { it[electric].toString() }
            orElse { fail() }
        }

        assertEquals("4", test(car))
        assertEquals("false", test(bicycle))
    }
}