[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.adokky/dyno-core)](https://mvnrepository.com/artifact/io.github.adokky/dyno-core)
[![javadoc](https://javadoc.io/badge2/io.github.adokky/dyno-core/javadoc.svg)](https://javadoc.io/doc/io.github.adokky/dyno-core)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

# DynamicObject

Type-safe, serializable, heterogeneous map.

## Key Features

- **Flexible and Type-Safe API**: Work with dynamic objects using strictly typed keys (`DynoKey<T>`).
- **Automatic JSON Serialization**: Out-of-the-box support for `Json` format of `kotlinx.serialization`.
- **Read-Only and Mutable Variants**: Choose between `DynamicObject` and `MutableDynamicObject`.
- **Schema-less**: No need to predefine schemas; properties can be added or removed dynamically.


## Quick Example

```kotlin
// Define typed keys
object Person {
    val name by dynoKey<String>()
    val age by dynoKey<Int>()
    val emails by dynoKey<List<String>?>()
}

val person = mutableDynamicObjectOf(
    Person.name with "Alex",
    Person.age with 42,
    Person.emails with listOf("alex@example.com")
)

// Access values in a type-safe manner
val name: String = person[Person.name]
val age: Int = person[Person.age]
val emails: List<String>? = person[Person.emails]

person[Person.age] = 31
person.remove(Person.emails)

// Serialization support
val json = Json.encodeToString(person)
val restored = Json.decodeFromString(json)
```

## Module `core`

`io.github.adokky:dyno-core:0.7`

### `DynoKey<T>`

A typed key used to access values in `DynamicObject` and `MutableDynamicObject`.
Can be instantiated directly or by using a delegate `dynoKey`.

```kotlin
object Person {
    val id by dynoKey<Int>("id")
    val name by dynoKey<String>("name")
    val age by dynoKey<Int?>("age")
}
```

### `DynamicObject`

The main class representing an immutable, type-safe, and serializable heterogeneous map. It allows storing and retrieving values of different types using typed keys (`DynoKey`).

**Example:**
```kotlin
val obj = dynamicObjectOf(
    Person.id with 42,
    Person.name with "Alex",
    Person.age with 30
)

val name: Int? = obj[Person.age]
```

If the type parameter `T` in `DynoKey<T>` is not nullable, then the return type is also non-nullable. 
`NoSuchDynoKeyException` is thrown if the key does not exist:

```kotlin
val id: Int = obj[Person.id]
```

### `MutableDynamicObject`

A mutable variant of `DynamicObject` that allows adding, updating, and removing key-value pairs. Shares the same type-safety and serialization features.

```kotlin
val obj = mutableDynamicObjectOf(Person.name with "Bob")
obj[Person.age] = 25
obj -= Person.name
```

## Module `classmap`

`io.github.adokky:dyno-classmap:0.7`

### `ClassMap<T>`

A specialized map implementation that associates `T` / `KClass<T>` keys with values of serializable type `T`.

```kotlin
val map: ClassMap = buildClassMap {
    put("foo")
    put(42)
}

map.get<String>() // returns "foo"
map.get<Int>() // returns 42
```

In `JSON`, serial name of `T` used as a key:

```kotlin
@Serializable
@SerialName("user")
data class User(val name: String, val age: Int)

@Serializable
@SerialName("account")
data class Account(val id: Int, val active: Boolean)

val map: ClassMap = buildClassMap {
    put(User("Alex", 30))
    put(Account(123, true))
}

Json.encodeToString(ClassMapSerializer, map)
```

```json
{
    "user": {
        "name": "Alex",
        "age": 30
    },
    "account": {
        "id": 123,
        "active": true
    }
}
```

For working with any `DynoMap` (including `DynamicObject`), functions like `getInstance`/`setInstance` are available, for example:

```kotlin
val obj = mutableDynamicObjectOf()
obj.setInstance(User("Bob", 25))
val user: User? = obj.getInstance()
```

These functions allow interacting with class instances without explicitly specifying keys. However:
* `ClassMap` only accept classes/types as the keys, while `DynoMap` is only restricted by its type argument.
* `ClassMap` provides more ergonomics by leveraging standard method names and built-in operators

### `TypedClassMap<T>`

A variant of ClassMap that restricts keys to only subclasses of a specified base class `T`.

```kotlin
abstract class Animal
@Serializable data class Dog(val name: String) : Animal()
@Serializable data class Cat(val name: String) : Animal()

val map: TypedClassMap<Animal> = buildTypedClassMap {
    put(Dog("Buddy"))
    put(Cat("Whiskers"))
}

val dog: Dog? = map.get<Dog>()
val cat: Cat? = map.get<Cat>()

// compilation error: String is not a subtype of Animal
map.put("string")
```

## Serialization

Dyno allows both lazy and eager deserialization modes,
allowing you to choose the approach that best fits your performance and usability requirements.

### Lazy (default)

* Works out-of-the-box: no additional setup required for basic usage.
* Schema-less by design: easily extendable without pre-registering keys.
* May be slower than eager deserialization, due to intermediate representation storage.

The following classes are automatically serializable without any extra steps:
- `DynamicObject`
- `MutableDynamicObject`
- `ClassMap`
- `MutableClassMap`

The following classes have type arguments, so you may need to specify an explicit serializer if type argument is not serializable:
- `DynoMap`
- `MutableDynoMap`
- `TypedClassMap`
- `MutableTypedClassMap`

> **Note:** Annotations like `@Serializable(with=Serializer::class)` or `@Contextual` do not work inside function type arguments. For example, this will not work: `Json.decodeFromString<TypedClassMap<@Contextual Any>>("{}")`

To serialize these classes using `Json.encodeToString` or `Json.decodeFromString`, you must specify an explicit serializer. For example, `MutableClassMap` should be serialized like this:
```kotlin
Json.encodeToString(MutableTypedClassMapSerializer, mutableTypedClassMap)
```

#### Lazy Serializer Mapping

| Class                 | Serializer                     | 
|-----------------------|--------------------------------| 
| TypedClassMap         | TypedClassMapSerializer        | 
| MutableTypedClassMap  | MutableTypedClassMapSerializer | 
| DynoMap               | DynoMapMapSerializer           | 
| MutableDynoMap        | MutableDynoMapSerializer       |


### Eager Deserialization

Achieved with `AbstractEagerDynoSerializer` — a powerful base class for implementing eager deserialization strategies.

- **Direct Deserialization**. Unlike lazy serialization, which stores intermediate `JsonElement` representations, eager serialization decodes values directly into their final types, offering better memory efficiency.
- **Mixed Strategies**: Supports mixing eager and lazy (`JsonElement`) deserialization strategies within the same object by returning `ResolveResult.Keep`.
- **Polymorphic Handling**: Delay deserialization of keys that depend on other fields using `ResolveResult.Delay`, enabling polymorphic or conditional deserialization.

Usage [example](docs/eager-deserialization.md).

## `DynoKey` validation

The `onAssign` and `onDecode` processors can be assigned to add validation logic.
All processors are chained in the order of assignment.

* `onAssign` is called when a value is manually assigned to the key (e.g., `obj[key] = value` or `dynamicObjectOf(key with value)`).
* `onDecode` is called when a value is deserialized. 
Usefulll when validation is only needed for deserialized objects received from network.
* `validate` assigns the same validation logic to both `onAssign` and `onDecode`.

### Example

```kotlin
object Person {
    val age by dynoKey<Int>().onDecode { 
        require(it > 0) { "'age' must be positive, but was: $it" } 
    }
    val name by dynoKey<String>().validate {
        require(it.isNotBlank()) { "'name' must not be empty" }
    }
}
```

Decoding from JSON - both `onDecode` and `validate` processors are called:

```kotlin
val obj = Json.decodeFromString<DynamicObject>("""{"name": "", "age": -1}""")
decoded[Person.age]  // throws IllegalArgumentException
decoded[Person.name] // throws IllegalArgumentException
```

Manual assignment - only `validate` processor is called:

```kotlin
val obj = mutableDynamicObjectOf(Person.age with -1)
obj[Person.age]       // returns -1
obj[Person.name] = "" // throws IllegalArgumentException

// throws IllegalArgumentException
mutableDynamicObjectOf(Person.name with "") 
```

### Composition

The validation functions are easily composable and can be used to build your own validation DSL:

```kotlin
fun <R: DynoKeySpec<String>> R.notBlank() = validate {
    require(it.isNotBlank()) { "property '$name' must not be empty" }
}

fun <R: DynoKeySpec<String>> R.maxLength(max: Int) = validate {
    require(it.length <= max) { "property '$name' length must be <= $max, but was: ${it.length}" }
}
```

Multiple validators are chained together:

```kotlin
object User {
    val name = DynoKey<String>("username")
        .notBlank()
        .maxLength(100)
        
    val email by dynoKey<String>()
        .validate { require("@" in it) { "property '$name' must be valid email" } }
        .maxLength(255)
}
```

When a value is assigned or decoded, all validators in the chain are executed in order.