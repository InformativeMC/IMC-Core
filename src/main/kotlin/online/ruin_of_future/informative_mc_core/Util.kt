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