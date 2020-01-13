package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.view.dialog.StartAnalysisDialog
import javafx.stage.StageStyle
import tornadofx.*

class RootView : View() {
    override val root = borderpane {
        title = "Disk space analyzer"
        setPrefSize(800.0, 600.0)
        top<AppMenu>()
        left<FileTreeView>()
        center<DirectoryView>()
        bottom<StatusBarView>()
    }

    init {
        find<StartAnalysisDialog>().openModal(stageStyle = StageStyle.UTILITY)
    }
}