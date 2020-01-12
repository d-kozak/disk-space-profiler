package io.dkozak.profiler.client

import io.dkozak.profiler.scanner.Library
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import javafx.stage.StageStyle
import tornadofx.*
import java.io.File

class RootView : View() {
    override val root = borderpane {
        title = "Disk space analyzer"
        minHeight = 600.0
        minWidth = 800.0
        top<AppMenu>()
        left<FileTreeView>()
        center<DirectoryView>()
        bottom<StatusBarView>()
    }
}

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

sealed class FileTreeEntry
data class RootEntry(val files: List<FileTreeEntry>) : FileTreeEntry() {
    constructor(vararg files: FileTreeEntry) : this(files.toList())
}

data class DirectoryEntry(val name: String, val files: List<FileTreeEntry>) : FileTreeEntry() {
    constructor(name: String, vararg files: FileTreeEntry) : this(name, files.toList())
}

data class FileEntry(val name: String) : FileTreeEntry()

class FileTreeView : View() {

    private val dummyFileRoot: FileTreeEntry = RootEntry(
            FileEntry("one.txt"),
            DirectoryEntry("dir1",
                    FileEntry("img.jpg"),
                    DirectoryEntry("inner dir",
                            FileEntry("secret.txt")
                    )
            ),
            DirectoryEntry("dir2",
                    FileEntry("img2.png")
            ),
            FileEntry("foo.dat")
    )

    override val root = treeview<FileTreeEntry> {
        root = TreeItem(dummyFileRoot)
        cellFormat {
            text = when (it) {
                is RootEntry -> "/"
                is DirectoryEntry -> it.name
                is FileEntry -> it.name
            }
        }

        populate { parent ->
            when (val node = parent.value) {
                is RootEntry -> node.files
                is DirectoryEntry -> node.files
                is FileEntry -> null
            }
        }

        contextmenu {
            item("Open")
            item("Refresh")
            item("Delete")
        }
    }

}

class DirectoryView : View() {

    private val dummyFiles = listOf("File 1", "File 2", "Dir 1", "File 3", "Dir 2").asObservable()

    override val root = vbox {
        alignment = Pos.CENTER
        label("dir1") {
            style {
                padding = box(5.px)
                fontWeight = FontWeight.BOLD
            }
        }
        listview(dummyFiles) {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            contextmenu {
                item("Open")
                item("Refresh")
                item("Delete")
            }
        }
    }
}

class StatusBarView : View() {
    override val root: Parent = borderpane {
        left = label("Last scan at DD/MM/YYYY")
        right = label("Used 23/50 gb")

        children.style {
            padding = box(2.px, 5.px)
        }
    }
}


class ProgressView : View() {
    val status: TaskStatus by inject()

    override val root = vbox(4) {
        alignment = Pos.CENTER
        visibleWhen { status.running }
        label(status.title).style { fontWeight = FontWeight.BOLD }
        hbox(4) {
            alignment = Pos.CENTER
            label(status.message)
            progressbar(status.progress)
            visibleWhen { status.running }
        }
    }
}

class StartAnalysisDialog : Fragment() {
    val status: TaskStatus by inject()

    val rootDir = SimpleStringProperty("/")

    override val root: Parent = vbox {
        borderpane {
            title = "Analysis configuration"
            setPrefSize(300.0, 75.0)

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
                                        //updateTitle("Analysis running")
                                        updateMessage("Init")
                                        updateProgress(3, 10)
                                        Thread.sleep(3000)
                                        updateMessage("Step 1/2")
                                        updateProgress(6, 10)
                                        Thread.sleep(3000)
                                        updateMessage("Step 2/2")
                                        updateProgress(9, 10)
                                        Thread.sleep(3000)
                                    } ui {
                                        // close()
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

class DiskSpaceProfiler : App(RootView::class)

fun main(args: Array<String>) {
    println(Library().greeting)
    launch<DiskSpaceProfiler>(args)
}
