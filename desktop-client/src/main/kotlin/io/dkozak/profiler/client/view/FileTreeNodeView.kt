package io.dkozak.profiler.client.view

import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import tornadofx.*

class FileTreeNodeView : Fragment() {

    val node: TreeItem<FsNode> by param()

    val spaceTaken: Double = node.value.size.toDouble() / node.value.root.value.occupiedSpace

    override val root: Parent = hbox {
        if (node.value is FsNode.DirectoryNode)
            imageview("folder.png")
        label(node.value.file.name)
        label("${node.value.size}B ")
        progressbar {
            progress = spaceTaken
        }
    }
}