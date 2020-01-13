package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.util.spaceColor
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import tornadofx.*


class FileTreeNodeView : Fragment() {

    val node: TreeItem<FsNode> by param()

    override val root: Parent = hbox(4) {
        if (node.value is FsNode.LazyNode) {
            label("...")
        } else {
            if (node.value is FsNode.DirectoryNode)
                imageview("folder.png")

            hbox(4) {
                label(node.value.file.name)
                if (node.parent != null && node.value.spaceTaken >= 0.1) {
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