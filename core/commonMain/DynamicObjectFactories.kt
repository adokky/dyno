package dyno


fun emptyDynamicObject(): DynamicObject = DynamicObject.Empty

/**
 * Creates a new [DynamicObject] using the provided [entries].
 *
 * Example:
 * ```
 * object Person {
 *     val name = DynoKey<String>("name")
 *     val age = DynoKey<Int>("age")
 *     val emails = DynoKey<List<String>>("emails")
 * }
 *
 * // Create a mutable dynamic object
 * val person = dynamicObjectOf(
 *     Person.name with "Alex",
 *     Person.age with 42,
 *     Person.emails with listOf("alex@example.com")
 * )
 * ```
 */
fun dynamicObjectOf(vararg entries: DynoEntry<DynoKey<*>, *>): DynamicObject =
    DynamicObjectImpl(entries.toList())

/**
 * Creates a new [DynamicObject] using the provided [entries].
 *
 * Example:
 * ```
 * object Person {
 *     val name = DynoKey<String>("name")
 *     val age = DynoKey<Int>("age")
 *     val emails = DynoKey<List<String>>("emails")
 * }
 *
 * // Create a mutable dynamic object
 * val person = dynamicObjectOf(listOf(
 *     Person.name with "Alex",
 *     Person.age with 42,
 *     Person.emails with listOf("alex@example.com")
 * ))
 * ```
 */
fun dynamicObjectOf(entries: Collection<DynoEntry<DynoKey<*>, *>>): DynamicObject =
    DynamicObjectImpl(entries)

/**
 * Creates a new [MutableDynamicObject] using the provided [entries].
 *
 * Example:
 * ```
 * object Person {
 *     val name = DynoKey<String>("name")
 *     val age = DynoKey<Int>("age")
 *     val emails = DynoKey<List<String>>("emails")
 * }
 *
 * // Create a mutable dynamic object
 * val person = mutableDynamicObjectOf(
 *     Person.name with "Alex",
 *     Person.age with 42,
 *     Person.emails with listOf("alex@example.com")
 * )
 * ```
 */
fun mutableDynamicObjectOf(vararg entries: DynoEntry<DynoKey<*>, *>): MutableDynamicObject =
    DynamicObjectImpl(entries.toList())

/**
 * Creates a new [MutableDynamicObject] using the provided [entries].
 *
 * Example:
 * ```
 * object Person {
 *     val name = DynoKey<String>("name")
 *     val age = DynoKey<Int>("age")
 *     val emails = DynoKey<List<String>>("emails")
 * }
 *
 * // Create a mutable dynamic object
 * val person = mutableDynamicObjectOf(listOf(
 *     Person.name with "Alex",
 *     Person.age with 42,
 *     Person.emails with listOf("alex@example.com")
 * ))
 * ```
 */
fun mutableDynamicObjectOf(entries: Collection<DynoEntry<DynoKey<*>, *>>): MutableDynamicObject =
    DynamicObjectImpl(entries)


fun dynamicObjectOf(): DynamicObject = DynamicObject.Empty

fun mutableDynamicObjectOf(): MutableDynamicObject = DynamicObjectImpl()

fun MutableDynamicObject(capacity: Int): MutableDynamicObject = DynamicObjectImpl(capacity)

/**
 * Creates a new [DynamicObject] using the provided [body] lambda.
 * Inside the lambda, you can populate the object with key-value pairs using the methods
 * available on [MutableDynamicObject] and [set] infix function.
 *
 * Example:
 * ```
 * object Person {
 *     val name = DynoKey<String>("name")
 *     val age = DynoKey<Int>("age")
 *     val emails = DynoKey<List<String>>("emails")
 * }
 *
 * // Create a mutable dynamic object
 * val person = buildDynamicObject {
 *     Person.name set "Alex"
 *     Person.age set 42
 *     Person.emails set listOf("alex@example.com")
 * }
 * ```
 */
inline fun buildDynamicObject(body: MutableDynamicObject.() -> Unit): DynamicObject =
    buildMutableDynamicObject(body)

/**
 * Creates a new [MutableDynamicObject] using the provided [body] lambda.
 * Inside the lambda, you can populate the object with key-value pairs using the methods
 * available on [MutableDynamicObject] and [set] infix function.
 *
 * Example:
 * ```
 * object Person {
 *     val name = DynoKey<String>("name")
 *     val age = DynoKey<Int>("age")
 *     val emails = DynoKey<List<String>>("emails")
 * }
 *
 * // Create a mutable dynamic object
 * val person = buildMutableDynamicObject {
 *     Person.name set "Alex"
 *     Person.age set 42
 *     Person.emails set listOf("alex@example.com")
 * }
 * ```
 */
inline fun buildMutableDynamicObject(body: MutableDynamicObject.() -> Unit): MutableDynamicObject =
    mutableDynamicObjectOf().apply(body)