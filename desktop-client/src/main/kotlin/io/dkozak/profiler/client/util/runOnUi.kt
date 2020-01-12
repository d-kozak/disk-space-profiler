package io.dkozak.profiler.client.util

import javafx.application.Platform

fun onUiThread(block: () -> Unit) = Platform.runLater(block)