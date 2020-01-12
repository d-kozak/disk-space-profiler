package io.dkozak.profiler.scanner.fs

import java.io.File

sealed class FsRoot
data class WindowsRoot(val disks: List<DiskRoot>) : FsRoot()
data class DiskRoot(val file: File, var node: FsNode.DirectoryNode) : FsRoot()