package korlibs.annotations

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR
)
annotation class ExperimentalKorlibsApi()
