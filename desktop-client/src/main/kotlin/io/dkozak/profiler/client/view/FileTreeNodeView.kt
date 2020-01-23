package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.util.spaceColor
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.isDirectory
import io.dkozak.profiler.scanner.fs.isLazyFile
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import tornadofx.*

/**
 * One node in the FileTreeView's treeview
 * @see FileTreeView
 */
class FileTreeNodeView : Fragment() {

    val node: TreeItem<FsNode> by param()

    override val root: Parent = hbox(4) {
        if (node.isLazyFile) {
            label("...loading...")
        } else {
            if (node.isDirectory)
                imageview("folder.png")

            hbox(4) {
                label(node.value.file.name)
                if (node.value.spaceTaken >= 0.05) {
                    progressbar {
                        progress = node.value.spaceTaken
                        maxWidth = 50.0
                        style {
                            accentColor = spaceColor(node.value.spaceTaken)
                        }
                    }
                }
                label(node.value.size.toString())
            }

        }
    }
}