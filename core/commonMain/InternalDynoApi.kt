package dyno

/**
 * Any API marked with this annotation is effectively internal,
 * which means it should not be used outside dyno.
 * Signature, semantics, source and binary compatibilities are not
 * guaranteed for this API and will be changed without any warnings
 * or migration aids.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
annotation class InternalDynoApi

/**
 * The behavior of such API may be changed or the API may be removed completely in any further release.
 * Beware using the annotated API especially if you're developing a library,
 * since your library might become binary incompatible with the future versions of Dyno.
 */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalDynoApi