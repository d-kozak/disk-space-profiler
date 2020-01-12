package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.view.dialog.StartAnalysisDialog
import javafx.scene.Parent
import javafx.stage.StageStyle
import tornadofx.*

class AppMenu : View() {
    override val root: Parent = menubar {
        menu("Analysis") {
            item("Run") {
                action {
                    find<StartAnalysisDialog>().openModal(stageStyle = StageStyle.UTILITY)
                }
            }
        }
        menu("Help") {
            item("Intructions")
            item("About")
        }
    }
}