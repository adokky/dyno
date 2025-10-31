# DynamicObject

Type-safe, serializable, heterogeneous map.

## Key Features

- **Flexible and Type-Safe API**: Work with dynamic objects using strictly typed keys (`DynamicObjectKey`).
- **Automatic Serialization**: Out-of-the-box support for `kotlinx.serialization` with both lazy and eager deserialization modes, 
allowing you to choose the approach that best fits your performance and usability requirements.
- **Immutable and Mutable Variants**: Choose between `DynamicObject` and `MutableDynamicObject`.
- **Schema-less by Default**: No need to predefine schemas; properties can be added or removed dynamically.
- **Efficient Memory Usage**: Optimized internal storage reduces memory overhead.


## Quick Example

```kotlin
// Define typed keys
object Person {
    val name = DynoKey<String>("name")
    val age = DynoKey<Int>("age")
    val emails = DynoKey<List<String>>("emails")
}

// Create a mutable dynamic object
val person = mutableDynamicObjectOf(
    Person.name with "Alex",
    Person.age with 42,
    Person.emails with listOf("alex@example.com")
)

// Access values in a type-safe manner
val name: String = person[Person.name]
val age: Int = person[Person.age]
val emails: List<String> = person[Person.emails]

// Modify values
person[Person.age] = 31
person.remove(Person.emails)

// Serialization support
val json = Json.encodeToString(person)
val restored = Json.decodeFromString(json)
```

## Setup

TODO