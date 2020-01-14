package io.dkozak.profiler.scanner.util

import java.text.DecimalFormat


/**
 * Transform Long into FileSize
 */
fun Long.toFileSize() = FileSize(this)

/**
 * Transform Int into FileSize
 */
val Int.bytes
    get() = FileSize(this.toLong())

/**
 * Represents size of a file or directory.
 */
@Cleanup("This class was extended only by what was necessary for this app(to be just good enough), still many operations missing")
inline class FileSize(
        /**
         * Size in bytes
         */
        val bytes: Long = 0
) : Comparable<FileSize> {

    override fun toString(): String = when {
        bytes >= TERA -> asUnit(TERA, "TB")
        bytes >= GIGA -> asUnit(GIGA, "GB")
        bytes >= MEGA -> asUnit(MEGA, "MB")
        bytes >= KILO -> asUnit(KILO, "KB")
        else -> "$bytes B"
    }

    override fun compareTo(other: FileSize): Int = this.bytes.compareTo(other.bytes)

    fun relativeTo(other: FileSize): Double = this.bytes.toDouble() / other.bytes

    operator fun plus(other: FileSize): FileSize = FileSize(bytes + other.bytes)
    operator fun minus(other: FileSize): FileSize = FileSize(bytes - other.bytes)
    operator fun unaryMinus(): FileSize = FileSize(-bytes)

    private fun asUnit(value: Long, unit: String) =
            "${numberFormat.format(bytes.toDouble() / value)} ${unit}"

}


private val KILO = Math.pow(10.0, 3.0).toLong()
private val MEGA = Math.pow(10.0, 6.0).toLong()
private val GIGA = Math.pow(10.0, 9.0).toLong()
private val TERA = Math.pow(10.0, 12.0).toLong()

private val numberFormat = DecimalFormat("#.00")