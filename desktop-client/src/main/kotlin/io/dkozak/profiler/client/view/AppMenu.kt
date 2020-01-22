package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.view.dialog.openAboutDialog
import io.dkozak.profiler.client.view.dialog.openInstructionsDialog
import io.dkozak.profiler.client.view.dialog.openStartScanDialog
import javafx.scene.Parent
import tornadofx.*

class AppMenu : View() {
    override val root: Parent = menubar {
        menu("Analysis") {
            item("Run") {
                action { openStartScanDialog() }
            }
        }
        menu("Help") {
            item("Instructions") {
                action { openInstructionsDialog() }
            }
            item("About") {
                action { openAboutDialog() }
            }
        }
    }
}