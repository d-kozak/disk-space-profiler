package io.dkozak.profiler.client.view.dialog

import javafx.event.EventTarget
import tornadofx.*

private val text = """
    To start an analysis, select Analysis->Run and specify disk/folder to be analyzed.
    
    The results are shown in two views. On the left, you will the tree structure.
    In the middle, you will see the content of currently selected folder. 
    Every file of significant size is decorated from blue to red based
    on how big it is.
    
    When the first analysis is done, rescan can be executed from any node by right-clicking 
    on it and selecting refresh.
    
    Any item can be deleted by right-clicking on it and selecting delete or by pressing 
    delete when the item is selected.
""".trimIndent()

class InstructionsDialog : InfoDialog() {


    override fun EventTarget.dialogContent() {
        label(text)
    }
}


