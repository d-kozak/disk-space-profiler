package io.dkozak.profiler.client.view


import io.dkozak.profiler.client.view.dialog.openStartScanDialog

import tornadofx.*

/**
 * Root view of the app
 */
class RootView : View() {
    override val root = borderpane {
        title = "Disk space analyzer"
        setPrefSize(1000.0, 600.0)
        top<AppMenu>()
        center {
            splitpane {
                add<FileTreeView>()
                add<DirectoryView>()
                setDividerPosition(0, 0.3)
            }
        }
        bottom<StatusBarView>()
    }

    init {
        openStartScanDialog()
    }
}