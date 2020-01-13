package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.view.dialog.StartAnalysisDialog
import javafx.stage.StageStyle
import tornadofx.*

class RootView : View() {
    override val root = borderpane {
        title = "Disk space analyzer"
        setPrefSize(800.0, 600.0)
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
        find<StartAnalysisDialog>().openModal(stageStyle = StageStyle.UTILITY)
    }
}