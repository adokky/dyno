[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.adokky/dyno)](https://mvnrepository.com/artifact/io.github.adokky/dyno)
[![javadoc](https://javadoc.io/badge2/io.github.adokky/dyno/javadoc.svg)](https://javadoc.io/doc/io.github.adokky/dyno)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

# DynamicObject

Type-safe, serializable, heterogeneous map.

## Key Features

- **Flexible and Type-Safe API**: Work with dynamic objects using strictly typed keys (`DynoKey<T>`).
- **Automatic Serialization**: Out-of-the-box support for `kotlinx.serialization` with both lazy and eager deserialization modes, 
allowing you to choose the approach that best fits your performance and usability requirements.
- **Read-Only and Mutable Variants**: Choose between `DynamicObject` and `MutableDynamicObject`.
- **Schema-less**: No need to predefine schemas; properties can be added or removed dynamically.


## Quick Example

```kotlin
// Define typed keys
object Person {
    val name = DynoKey<String>("name")
    val age = DynoKey<Int>("age")
    val emails = DynoKey<List<String>>("emails")
}

val person = mutableDynamicObjectOf(
    Person.name with "Alex",
    Person.age with 42,
    Person.emails with listOf("alex@example.com")
)

// Access values in a type-safe manner
val name: String = person[Person.name]
val age: Int = person[Person.age]
val emails: List<String> = person[Person.emails]

person[Person.age] = 31
person.remove(Person.emails)

// Serialization support
val json = Json.encodeToString(person)
val restored = Json.decodeFromString(json)
```

## Module `core`

`io.github.adokky:dyno-core:0.7`

### `DynoKey<T>` and `RequiredDynoKey<T>`

A typed key used to access values in `DynamicObject` and `MutableDynamicObject`.

```kotlin
object Person {
    val id = RequiredDynoKey<Int>("id")
    val name = DynoKey<String>("name")
    val age = DynoKey<Int>("age")
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

val name: String? = obj[Person.name] // "Alex"
```

If the key is `RequiredDynoKey`, then the return type is non-nullable. `NoSuchDynoKeyException` is thrown if the key does not exist:

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

// This will cause compilation error, because "string" is not a subclass of Animal
map.put("string")
```

### `DynoMap`

A flexible, schema-less map-like structure that underlies both `DynamicObject` and `MutableDynamicObject`. It provides the core mechanics for storing and retrieving values by `DynoKey`.

## Serialization

### Lazy (default)

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

**Example:**

```kotlin
object PersonEagerSerializer : AbstractEagerDynoSerializer<MutableDynamicObject>() {
    object Vehicle {
        val type = DynoKey<String>("type")
        val brand = DynoKey<String>("brand")
        val engineType = DynoKey<String>("engineType") // only for Car
        val gearsCount = DynoKey<Int>("gearsCount")   // only for Bicycle
    }

    override fun resolve(context: ResolveContext): ResolveResult {
        return when (context.keyString) {
            "type" -> Vehicle.type
            "brand" -> Vehicle.brand
            "engineType" -> if (context.getDecoded(Vehicle.type) == "car") Vehicle.engineType else ResolveResult.Delay
            "gearsCount" -> if (context.getDecoded(Vehicle.type) == "bicycle") Vehicle.gearsCount else ResolveResult.Delay
            else -> ResolveResult.Skip
        }
    }

    // At this stage, all keys are scanned 
    override fun postResolve(context: ResolveContext): ResolveResult {
        val key = context.keyString
        val type = context.getDecoded(Vehicle.type)
        return when (context.keyString) {
            key == "engineType" && type == "car" -> Vehicle.engineType
            key == "gearsCount" && type == "bicycle" ->  Vehicle.gearsCount
            else -> ResolveResult.Skip
        }
    }

    override fun createNew(): MutableDynamicObject = MutableDynamicObject()
}