package io.dkozak.profiler.client.util

import io.dkozak.profiler.scanner.util.Precondition
import javafx.scene.paint.Color

/**
 * Return color based on how much space is given file taking.
 */
fun spaceColor(@Precondition("0 <= n <= 1") n: Double): Color {
    check(0 <= n && n <= 1) { "Value $n should be between 0 and 1" }
    return when {
        n >= 0.7 -> Color.RED
        n >= 0.4 -> Color.ORANGE
        else -> Color.BLUE
    }
}