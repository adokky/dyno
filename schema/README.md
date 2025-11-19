## Module `schema`

Schemas provide several advantages over plain `DynamicObject`:

- **Structural Validation**: Automatic validation of required fields and types during deserialization
- **Eager Deserialization**: All fields are validated and deserialized immediately, catching errors early
- **Type Safety**: Using `Entity<Schema>` instead of `DynamicObject` provides compile-time type safety

```kotlin
implementation("io.github.adokky:dyno-schema:0.7")
```

### Schema Definition for `DynoMap`

```kotlin
object Person: SimpleDynoSchema("person") {
    val name by dynoKey<String>()
    val age by dynoKey<Int>()
}
```

Schema object implements `KSerializer` for corresponding `DynoMap`. Declare type alias to reuse inside serializable classes:

```kotlin
private typealias PersonEntity = @Serializable(Person::class) DynoMap<SchemaProperty<Person, *>>

@Serializable
data class SomeData(val person: PersonEntity)
```

You can use `new` helper to instantiate the map: 

```kotlin
val person: PersonEntity = Person.new {
    name set "Alex"
    age set 100
}
```

If you forgot to set a required property, `IllegalStateException` is thrown:

```kotlin
// IllegalStateException: Schema 'person' requires the following properties: name
Person.new {
    age set 100
}
```

There is unsafe version of new for cases where you want to bypass validation (e.g., partial initialization):

```kotlin
// completes without exception
Person.newUnsafe {
    age set 100
}
```

### Polymorphic Schema

Define polymorphic schema:

```kotlin
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
```

Define polymorphic hierarchy:

```kotlin
val json = Json {
    serializersModule = SerializersModule {
        dynoSchemaRegistry {
            polymorphic(Vehicle) {
                schema(Bicycle)
                schema(Car)
                // specify default schema for case with absent discriminator
                fallback = Car
            }
        }
    }
}
```

Define polymorphic serializable entity:

```kotlin
private typealias VehicleEntity = @Serializable(Vehicle.Companion::class) Entity<Vehicle>
```

Deserialize:

```kotlin
@Serializable
private data class VehicleHolder(val vehicle: VehicleEntity)

val holder = json.decodeFromString<VehicleHolder>(
    """{"vehicle":{"type":"car","name":"Toyota","wheels":4}}"""
)
```

Use DSL for type switch:

```kotlin
val description = holder.vehicle.selectSingle {
    on<Car> { "car with ${it[wheels]} wheels" }
    on<Bicycle> { if (it[electric]) "electric bike" else "bicycle" }
    orElse { "unrecognized vehicle '${it[name]}'" }
}
println(description)
```