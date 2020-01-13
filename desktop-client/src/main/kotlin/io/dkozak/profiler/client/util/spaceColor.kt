package io.dkozak.profiler.client.util

import javafx.scene.paint.Color

fun spaceColor(n: Double) = when {
    n >= 0.7 -> Color.RED
    n >= 0.4 -> Color.ORANGE
    else -> Color.BLUE
}