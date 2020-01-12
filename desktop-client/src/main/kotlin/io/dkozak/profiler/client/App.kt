package io.dkozak.profiler.client

import io.dkozak.profiler.client.view.RootView
import tornadofx.*

class DiskSpaceProfiler : App(RootView::class)

fun main(args: Array<String>) {
    launch<DiskSpaceProfiler>(args)
}
