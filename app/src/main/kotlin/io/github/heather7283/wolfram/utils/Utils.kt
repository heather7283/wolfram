package io.github.heather7283.wolfram.utils

import java.text.DecimalFormat

// To allow comparing numbers to 0 with ! (get fucked type system purists)
operator fun Number.not() = this.toDouble() == 0.0

// Can't believe this is not in stdlib
fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${DecimalFormat("0.#").format(kb)} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${DecimalFormat("0.#").format(mb)} MB"
    return "${DecimalFormat("0.#").format(mb / 1024.0)} GB"
}

// A -> B
infix fun Boolean.implies(other: Boolean) = (!this || other)
