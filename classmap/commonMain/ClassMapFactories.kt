package dyno

// Empty

fun emptyClassMap(): ClassMap = ClassMap.Empty

fun <Base: Any> emptyTypedClassMap(): TypedClassMap<Base> = TypedClassMap.Empty()


// Read-only

inline fun buildClassMap(body: MutableClassMap.() -> Unit): ClassMap =
    buildMutableClassMap(body)

inline fun <Base: Any> buildTypedClassMap(body: MutableTypedClassMap<Base>.() -> Unit): TypedClassMap<Base> =
    buildMutableTypedClassMap(body)


// Mutable

inline fun buildMutableClassMap(body: MutableClassMap.() -> Unit): MutableClassMap =
    MutableClassMap().apply(body)

inline fun <Base: Any> buildMutableTypedClassMap(body: MutableTypedClassMap<Base>.() -> Unit): MutableTypedClassMap<Base> =
    MutableTypedClassMap<Base>().apply(body)