package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.view.dialog.AboutDialog
import io.dkozak.profiler.client.view.dialog.InstructionsDialog
import io.dkozak.profiler.client.view.dialog.StartScanDialog
import javafx.scene.Parent
import javafx.stage.StageStyle
import tornadofx.*

class AppMenu : View() {
    override val root: Parent = menubar {
        menu("Analysis") {
            item("Run") {
                action {
                    find<StartScanDialog>().openModal(stageStyle = StageStyle.UTILITY)
                }
            }
        }
        menu("Help") {
            item("Instructions") {
                action {
                    find<InstructionsDialog>().openModal(stageStyle = StageStyle.UTILITY)
                }
            }
            item("About") {
                action {
                    find<AboutDialog>().openModal(stageStyle = StageStyle.UTILITY)
                }
            }
        }
    }
}