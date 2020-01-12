package io.dkozak.profiler.scanner.fs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

class FsNodeTest {

    @Nested
    inner class EqualsTest {


        @Test
        fun simpleEquals() {
            val f1 = FsNode.FileNode(File("."))
            val f2 = FsNode.FileNode(File("."))
            assertThat(f1).isEqualTo(f2)
        }
    }
}