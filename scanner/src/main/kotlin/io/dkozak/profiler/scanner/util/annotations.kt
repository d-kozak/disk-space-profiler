package io.dkozak.profiler.scanner.util

/**
 * Denotes a piece a code that should be refactored.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD,
        AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Repeatable
annotation class Cleanup(val text: String)


/**
 * Denotes an invariant that should hold for a given piece of code
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD,
        AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Repeatable
annotation class Invariant(val text: String)

/**
 * Denotes a precondition for a given function or its parameter.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Repeatable
annotation class Precondition(val text: String)

/**
 * Denotes a function or a class (in that case it applies to all its methods) that should be executed only from the background thread.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class BackgroundThread

/**
 * Denotes a function or a class (in that case it applies to all its methods) that should be executed only from the ui thread.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class UiThread