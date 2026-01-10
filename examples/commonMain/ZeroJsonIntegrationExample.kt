package dyno.example.zerojson

import dev.dokky.zerojson.JsonInline
import dyno.DynamicObject
import dyno.dynamicObjectOf
import dyno.dynoKey
import dyno.with
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object DynoKeys {
	val tags by dynoKey<List<String>>()
}

@Serializable
data class Employee(
	val name: String,
	val age: Int,
	@JsonInline val extra: DynamicObject
)

fun main() {
	val employee = Json.decodeFromString<Employee>(
		"""{
			"name": "Alex", 
            "tags": ["a", "b", "c"],
			"age": 55
		}"""
	)

    check(employee.extra[DynoKeys.tags] == listOf("a", "b", "c"))

	val encoded = Json.encodeToString(
		Employee(
			name = "Bob",
			age = 14,
			extra = dynamicObjectOf(DynoKeys.tags with listOf("x", "y", "z"))
		)
	)

    check(encoded == """{"name":"Bob","age":14,"tags":["x","y","z"]}""")
}