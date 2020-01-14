package io.dkozak.profiler.client.util

import javafx.application.Platform

/**
 * Execute a block of code on the JavaFX UI thread.
 */
fun onUiThread(block: () -> Unit) = Platform.runLater(block)