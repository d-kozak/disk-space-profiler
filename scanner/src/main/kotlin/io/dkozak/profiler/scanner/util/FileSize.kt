package io.dkozak.profiler.scanner.util

import java.text.DecimalFormat


private val KILO = Math.pow(10.0, 3.0).toLong()
private val MEGA = Math.pow(10.0, 6.0).toLong()
private val GIGA = Math.pow(10.0, 9.0).toLong()
private val TERA = Math.pow(10.0, 12.0).toLong()

private val numberFormat = DecimalFormat("#.00")

fun Long.toFileSize() = FileSize(this)
val Int.bytes
    get() = FileSize(this.toLong())

inline class FileSize(val bytes: Long = 0) : Comparable<FileSize> {

    override fun toString(): String = when {
        bytes >= TERA -> toUnit(TERA, "TB")
        bytes >= GIGA -> toUnit(GIGA, "GB")
        bytes >= MEGA -> toUnit(MEGA, "MB")
        bytes >= KILO -> toUnit(KILO, "KB")
        else -> "$bytes B"
    }

    override fun compareTo(other: FileSize): Int = this.bytes.compareTo(other.bytes)

    fun relativeTo(other: FileSize): Double = this.bytes.toDouble() / other.bytes

    operator fun plus(other: FileSize): FileSize = FileSize(bytes + other.bytes)

    private fun toUnit(value: Long, unit: String) =
            "${numberFormat.format(bytes.toDouble() / value)} ${unit}"
}