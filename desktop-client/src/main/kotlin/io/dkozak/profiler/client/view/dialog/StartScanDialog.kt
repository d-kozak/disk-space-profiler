package io.dkozak.profiler.client.view.dialog

import io.dkozak.profiler.client.event.MessageEvent
import io.dkozak.profiler.client.view.ProgressView
import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.ScanConfig
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import javafx.stage.StageStyle
import tornadofx.*
import java.io.File

fun UIComponent.openStartScanDialog() {
    find<StartScanDialog>().openModal(stageStyle = StageStyle.UTILITY)
}

/**
 * Dialog for starting new scan.
 */
class StartScanDialog : Fragment() {
    private val status: TaskStatus by inject()

    private val fileTreeViewModel: FileTreeViewModel by inject()

    private val formViewModel = ViewModel()

    /**
     * From where to start the scan
     */
    private val rootDir = formViewModel.bind { SimpleStringProperty("/") }
    /**
     * How deep should be the tree created internally
     */
    private val treeDepth = formViewModel.bind { SimpleIntegerProperty(ScanConfig.DEFAULT_TREE_DEPTH) }


    override val root: Parent = vbox {
        borderpane {
            title = "Run analysis"
            setPrefSize(400.0, 100.0)

            style {
                padding = box(5.px)
            }

            center {
                form {
                    fieldset("Analysis configuration") {
                        enableWhen(status.running.not())

                        field("Root directory") {
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

                        field("Tree depth") {
                            textfield(treeDepth).validator {
                                if (it?.toIntOrNull() != null) null else error("Integer expected")
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
                                        fire(MessageEvent("Scan cancelled"))
                                    } else {
                                        close()
                                    }
                                }
                            }
                            button("Start") {
                                enableWhen(formViewModel.valid.and(status.running.not()))
                                action {
                                    fileTreeViewModel.newScan(ScanConfig(treeDepth.value.toInt(), TreeItem(FsNode.DirectoryNode(File(rootDir.value)))))
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