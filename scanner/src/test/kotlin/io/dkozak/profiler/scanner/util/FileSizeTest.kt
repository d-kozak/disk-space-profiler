package io.dkozak.profiler.scanner.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class FileSizeTest {

    @Nested
    inner class ToStringTest {

        @Test
        fun `just bytes`() {
            assertThat(FileSize(100).toString())
                    .isEqualTo("100 B")
        }

        @Test
        fun `some megabytes`() {
            assertThat(FileSize(122_340_000).toString())
                    .isEqualTo("122.34 MB")
        }

        @Test
        fun `some gigabytes`() {
            assertThat(FileSize(5_122_340_000).toString())
                    .isEqualTo("5.12 GB")
        }
    }

}