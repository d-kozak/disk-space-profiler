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

    private val rootPathViewModel = ViewModel()

    private val rootDir = rootPathViewModel.bind { SimpleStringProperty("/") }


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
                    textfield(rootDir).validator {
                        val file = File(it)
                        when {
                            !file.exists() -> error("File does not exist")
                            !file.isDirectory -> error("File is not a directory")
                            else -> null
                        }
                    }
                    button("Select") {
                        action {
                            val initialDir = File(rootDir.value)
                            val dir = chooseDirectory("Select root directory", initialDirectory = if (initialDir.exists()) initialDir else null)
                            if (dir != null) {
                                rootDir.value = dir.absolutePath
                            }
                        }
                    }

                }
            }

            bottom {
                borderpane {
                    left {
                        add<ProgressView>(ProgressView::showMessage to false)
                    }
                    right {
                        hbox(4) {
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
                                enableWhen(rootPathViewModel.valid.and(status.running.not()))
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