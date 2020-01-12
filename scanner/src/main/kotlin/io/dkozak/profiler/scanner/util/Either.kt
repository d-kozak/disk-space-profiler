package io.dkozak.profiler.scanner.util


sealed class Either<A, B>
data class Left<A, B>(val value: A) : Either<A, B>()
data class Right<A, B>(val value: B) : Either<A, B>()