package online.ruin_of_future.informative_mc_core.util

import com.google.common.collect.HashBiMap

fun segmentLineString(len: Int = 60, withNewLine: Boolean = true): String {
    val sb = StringBuilder()
    for (i in 0 until len) {
        sb.append('=')
    }
    if (withNewLine) {
        sb.append('\n')
    }
    return sb.toString()
}

fun String.splitIfNeeded(width: Int, occupied: Int): List<String> {
    val res = mutableListOf<String>()
    if (this.length + occupied > width) {
        var l = 0
        var r = width - occupied
        while (l < this.length) {
            res.add(this.substring(l, r))
            l = r
            r = (r + width - occupied).coerceAtMost(this.length)
        }
    } else {
        res.add(this)
    }
    return res
}

// TODO: Add color options
enum class VirtualConsoleOption {
    Centered,
}

private const val magicConsoleToken = "#(@_@)#"

private val magicConsoleOp = run {
    val map = HashBiMap.create<String, VirtualConsoleOption>()
    map["--"] = VirtualConsoleOption.Centered
    return@run map
}

fun boxedConsoleString(raw: List<String>, width: Int = 60): String {
    val leftOccupied = 2
    val rightOccupied = 2
    val usableWidth = width - leftOccupied - rightOccupied
    val sb = StringBuilder()
    sb.append(segmentLineString(width))
    raw.forEach {
        it.splitIfNeeded(width, leftOccupied + rightOccupied).forEach { strRaw ->
            var str = strRaw.substringAfter(magicConsoleToken)
            when (magicConsoleOp[strRaw.substringBefore(magicConsoleToken)]) {
                VirtualConsoleOption.Centered -> {
                    val titleSb = StringBuilder()
                    val l = (usableWidth - str.length) / 2
                    val r = usableWidth - str.length - l
                    for (i in 0 until l) {
                        titleSb.append(' ')
                    }
                    titleSb.append(str)
                    for (i in 0 until r) {
                        titleSb.append(' ')
                    }
                    str = titleSb.toString()
                }
                else -> {

                }
            }
            sb.append("| $str")
            for (i in 0 until usableWidth - str.length) {
                sb.append(' ')
            }
            sb.append(" |\n")
        }
    }
    sb.append(segmentLineString(width, false))
    return sb.toString()
}

fun String.inConsole(option: VirtualConsoleOption): String {
    return "${magicConsoleOp.inverse()[option] ?: ""}$magicConsoleToken$this"
}