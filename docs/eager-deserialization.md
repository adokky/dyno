## Eager Deserialization Example

The code example shows how to implement eager deserialization using `AbstractEagerDynoSerializer` for a `MutableDynamicObject`. 

It defines typed keys for a `Vehicle` object with conditional fields (`engineType` for "car", `gearsCount` for "bicycle"). 

The `resolve` method determines which keys to deserialize immediately, delay, or skip based on the value of the `type` field. 

The `postResolve` method handles any additional logic after all keys are scanned.

```kotlin
object PersonEagerSerializer : AbstractEagerDynoSerializer<MutableDynamicObject>() {
    object Vehicle {
        val type by dynoKey<String>()
        val brand by dynoKey<String>()
        val engineType by dynoKey<String>() // only for Car
        val gearsCount by dynoKey<Int>()    // only for Bicycle
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
```