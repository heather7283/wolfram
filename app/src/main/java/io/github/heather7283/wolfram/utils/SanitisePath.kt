package io.github.heather7283.wolfram.utils

fun sanitisePath(input: String): String {
    // thank you android for this stupid nonsense
    val badChars = setOf('|', '?', '*', '<', '>', '"', ':', '+', '[', ']', '/', '\\', '%')
    val res = StringBuilder()

    for (c in input) {
        if (c in badChars) {
            res.append('%', c.code.toHexString(HexFormat.UpperCase))
        } else {
            res.append(c)
        }
    }

    return res.toString()
}
