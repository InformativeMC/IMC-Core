package online.ruin_of_future.informative_mc_core

import java.io.File
import java.io.IOException

@Suppress("UnUsed")
val FILE_ROOT = run {
    val path = System.getProperty("user.dir")
    if (path == null || path.isEmpty()) {
        throw IOException("Cannot access current working directory")
    } else {
        if (path.endsWith(File.separatorChar)) {
            path.trimEnd(File.separatorChar)
        }
        path
    }
}

fun getFile(root: String, path: String): File {
    return File("$root${File.separatorChar}$path")
}

fun saveToFile(content: String, file: File) {
    file.writeBytes(content.toByteArray())
}

fun Long.humanReadableSize(): String {
    val num = this
    val kb = 1L shl 10
    val mb = 1L shl 20
    val gb = 1L shl 30
    val tb = 1L shl 40
    return if (num < kb) {
        "$num B"
    } else if (num < mb) {
        val f = String.format("%.2f", num.toDouble() / kb)
        "$f KB"
    } else if (num < gb) {
        val f = String.format("%.2f", num.toDouble() / mb)
        "$f MB"
    } else if (num < tb) {
        val f = String.format("%.2f", num.toDouble() / gb)
        "$f GB"
    } else {
        val f = String.format("%.2f", num.toDouble() / tb)
        "$f TB"
    }
}

fun Int.humanReadableSize(): String {
    return this.toLong().humanReadableSize()
}