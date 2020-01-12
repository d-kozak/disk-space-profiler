package io.dkozak.profiler.client.view.dialog

import io.dkozak.profiler.client.view.ProgressView
import io.dkozak.profiler.client.viewmodel.runScan
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Parent
import tornadofx.*
import java.io.File

class StartAnalysisDialog : Fragment() {
    val status: TaskStatus by inject()

    val rootDir = SimpleStringProperty("/")

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
                                        runScan(rootDir.value)
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