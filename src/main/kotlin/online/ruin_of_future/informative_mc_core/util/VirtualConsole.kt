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

enum class VirtualConsoleOption {
    Cyan,
    Red,
    Yellow,
    BrightYellow,
    Blue,
    BrightBlue,
    Magenta,
    BrightCyan,
    Green,
    Reset,
}

class ConsoleLineSegment(
    val content: String,
    val option: VirtualConsoleOption,
) {
    fun build(): String {
        val sb = StringBuilder()
        sb.append(magicConsoleOp[option])
        sb.append(content)
        sb.append(magicConsoleOp[VirtualConsoleOption.Reset])
        return sb.toString()
    }

    fun asSingleLine(isCentered: Boolean = false): ConsoleLine {
        val line = ConsoleLine(isCentered)
        line.add(this)
        return line
    }
}

class ConsoleLine(private val isCentered: Boolean = false) : ArrayList<ConsoleLineSegment>() {
    fun splitIfNeeded(width: Int): List<ConsoleLine> {
        val lines = mutableListOf<ConsoleLine>()
        var cursor = 0
        val curLine = ConsoleLine(isCentered)
        this.forEach {
            var pos = 0
            while (it.content.length >= pos + width - cursor) {
                curLine.add(
                    ConsoleLineSegment(
                        it.content.substring(pos, pos + width - cursor),
                        it.option
                    )
                )
                pos += width - cursor
                cursor = 0
                // Copy
                val cpyLine = ConsoleLine(isCentered)
                curLine.forEach { seg -> cpyLine.add(seg) }
                lines.add(cpyLine)
                curLine.clear()
            }
            curLine.add(
                ConsoleLineSegment(
                    it.content.substring(pos, it.content.length),
                    it.option
                )
            )
            cursor += it.content.length - pos
        }
        lines.add(curLine)
        return lines
    }

    fun buildLine(width: Int): String {
        val sb = StringBuilder()
        val innerSb = StringBuilder()
        var len = 0
        this.forEach {
            innerSb.append(it.build())
            len += it.content.length
        }
        val remain = width - len
        if (isCentered) {
            val l = remain / 2
            val r = remain - l
            for (i in 0 until l) {
                sb.append(' ')
            }
            sb.append(innerSb.toString())
            for (i in 0 until r) {
                sb.append(' ')
            }
        } else {
            sb.append(innerSb.toString())
            for (i in 0 until remain) {
                sb.append(' ')
            }
        }
        return sb.toString()
    }
}

private val magicConsoleOp: HashBiMap<VirtualConsoleOption, String> = run {
    val map = HashBiMap.create<VirtualConsoleOption, String>()
    map[VirtualConsoleOption.Red] = "\u001B[31m"
    map[VirtualConsoleOption.Green] = "\u001B[32m"
    map[VirtualConsoleOption.Yellow] = "\u001B[33m"
    map[VirtualConsoleOption.Blue] = "\u001B[34m"
    map[VirtualConsoleOption.Magenta] = "\u001B[35m"
    map[VirtualConsoleOption.Cyan] = "\u001B[36m"
    map[VirtualConsoleOption.BrightCyan] = "\u001B[96m"
    map[VirtualConsoleOption.Reset] = "\u001B[39m"
    map[VirtualConsoleOption.BrightYellow] = "\u001B[93m"
    map[VirtualConsoleOption.BrightBlue] = "\u001B[94m"
    return@run map
}

fun boxedConsoleString(lines: List<ConsoleLine>, width: Int = 60): String {
    val leftOccupied = 2
    val rightOccupied = 2
    val usableWidth = width - leftOccupied - rightOccupied
    val sb = StringBuilder()
    val segLine = ConsoleLineSegment(segmentLineString(width), VirtualConsoleOption.Blue).build()
    sb.append(segLine)
    lines.forEach {
        it.splitIfNeeded(usableWidth).forEach { line ->
            sb.append(ConsoleLineSegment("| ", VirtualConsoleOption.Blue).build())
            sb.append(line.buildLine(usableWidth))
            sb.append(ConsoleLineSegment(" |\n", VirtualConsoleOption.Blue).build())
        }
    }
    sb.append(segLine)
    return sb.toString()
}