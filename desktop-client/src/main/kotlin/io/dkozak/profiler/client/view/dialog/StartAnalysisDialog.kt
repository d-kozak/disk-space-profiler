package io.dkozak.profiler.client.view.dialog

import io.dkozak.profiler.client.view.ProgressView
import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Parent
import tornadofx.*
import java.io.File

class StartAnalysisDialog : Fragment() {
    private val status: TaskStatus by inject()

    private val fileTreeViewModel: FileTreeViewModel by inject()

    private val rootDir = SimpleStringProperty("/")

    override val root: Parent = vbox {
        borderpane {
            title = "Analysis configuration"
            setPrefSize(400.0, 100.0)

            style {
                padding = box(5.px)
            }

            center {
                vbox(4) {
                    alignment = Pos.CENTER
                    enableWhen(status.running.not())
                    label("Root directory: ")
                    textfield(rootDir)
                    button("Select") {
                        action {
                            val initialDir = File(rootDir.value)
                            val dir = chooseDirectory("Select root directory", initialDirectory = if (initialDir.exists()) initialDir else null)
                            if (dir != null) {
                                rootDir.set(dir.absolutePath)
                            }
                        }
                    }

                }
            }

            bottom {
                borderpane {
                    left<ProgressView>()
                    right {
                        hbox {
                            alignment = Pos.BOTTOM_RIGHT
                            button("Cancel") {
                                action {
                                    if (status.running.value) {
                                        status.item.cancel()
                                    } else {
                                        close()
                                    }
                                }
                            }
                            button("Start") {
                                enableWhen(status.running.not())
                                action {
                                    runAsync {
                                        fileTreeViewModel.newScan(rootDir.value, this)
                                    } ui {
                                        close()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}