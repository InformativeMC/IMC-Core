package online.ruin_of_future.informative_mc_core.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@OptIn(ExperimentalSerializationApi::class)
object FileHanlder {
    val LOGGER = LogManager.getLogger("IMC Config")!!

    fun getFile(root: String, path: String): File {
        return File("$root${File.separatorChar}$path")
    }

    fun getFile(absolutePath: String): File {
        return File(absolutePath)
    }

    fun getFile(path: Path): File {
        return File(path.absolutePathString())
    }

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> saveToFile(content: T, file: File) {
        file.writeText(Json.encodeToString(content))
    }

    inline fun <reified T> saveToFile(content: T, filePath: String) {
        saveToFile(content, File(filePath))
    }

    inline fun <reified T> load(
        path: Path,
        default: T,
        createAndWriteIfAbsent: Boolean = true
    ): T {
        val file = getFile(path)
        val obj = if (file.exists()) {
            try {
                Json.decodeFromStream(file.inputStream())
            } catch (e: Exception) {
                val cpyFile = File("${file.absoluteFile.parent}${File.separatorChar}OLD-${file.name}")
                cpyFile.writeBytes(file.readBytes())
                LOGGER.warn("Error occurs when reading file. Creating a default config instead.")
                LOGGER.warn("Old config file would be renamed:")
                LOGGER.warn("\t${file.absolutePath}")
                LOGGER.warn("\t\t||")
                LOGGER.warn("\t\t\\/")
                LOGGER.warn("\t${cpyFile.absolutePath}")
                default
            }
        } else {
            if (createAndWriteIfAbsent) {
                LOGGER.info("Load $path failed. Creating a default one...")
                if (file.createNewFile()) {
                    saveToFile(default, file)
                } else {
                    LOGGER.error("Creating default file failed.")
                }
            }
            default
        }
        return obj
    }

    private fun save(path: Path) {
        Files.newBufferedWriter(path).use { writer ->
            // TODO: save config
        }
    }
}