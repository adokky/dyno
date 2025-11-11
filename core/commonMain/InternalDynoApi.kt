package dyno

/**
 * Any API marked with this annotation is effectively internal,
 * which means it should not be used outside `dyno`.
 *
 * Signature, semantics, source and binary compatibilities are not
 * guaranteed for this API and will be changed without any warnings
 * or migration aids.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
annotation class InternalDynoApi

/**
 * Any API marked with this annotation may be changed or be removed completely in any further release.
 *
 * Beware using the annotated API especially if you're developing a library,
 * since your library might become binary incompatible with the future versions of `dyno`.
 */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalDynoApi

/**
 * Using an API marked with this annotation can break type-safety and API contracts,
 * which may cause runtime errors.
 *
 * This annotation indicates that the API requires careful usage due to potential
 * safety issues, but does not imply any specific compatibility guarantees.
 * For compatibility information, refer to other annotations such as
 * [InternalDynoApi] and [ExperimentalDynoApi].
 */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class UnsafeDynoApi