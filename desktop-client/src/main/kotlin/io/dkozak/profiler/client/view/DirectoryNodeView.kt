package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.util.spaceColor
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.isDirectory
import io.dkozak.profiler.scanner.fs.isLazy
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority
import tornadofx.*

/**
 * One cell in the DirectoryView's listview
 * @see DirectoryView
 */
class DirectoryNodeView : Fragment() {

    val node: TreeItem<FsNode> by param()

    override val root: Parent = hbox(4) {
        if (node.isLazy) {
            label("...loading...")
        } else {
            if (node.isDirectory)
                imageview("folder.png")
            label(node.value.file.name)

            hbox(4) {
                alignment = Pos.CENTER_RIGHT
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
                if (node.value.spaceTaken >= 0.05)
                    progressbar {
                        progress = node.value.spaceTaken
                        style {
                            accentColor = spaceColor(node.value.spaceTaken)
                        }
                    }
                label(node.value.size.toString())
            }

        }
    }

}