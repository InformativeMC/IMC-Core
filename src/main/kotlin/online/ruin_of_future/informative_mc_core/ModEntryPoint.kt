package online.ruin_of_future.informative_mc_core

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import online.ruin_of_future.informative_mc_core.web_api.ApiServer
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.util.*

val cwd = run {
    // TODO: Replace with a more general way to detect correct directory.
    val cwd = System.getProperty("user.dir")
    if (cwd == null || cwd.isEmpty()) {
        throw IOException("Cannot access current working directory")
    } else {
        if (cwd.endsWith(File.separatorChar)) {
            cwd.trimEnd(File.separatorChar)
        }
    }
    cwd
}

val modConfigDirPath = "$cwd${File.separatorChar}config${File.separatorChar}InformativeMC"
val modConfigFilePath = "$modConfigDirPath${File.separatorChar}IMC-Core.json"

val modDataDirPath = "$cwd${File.separatorChar}mods${File.separatorChar}InformativeMC"
val modDataFilePath =
    "$cwd${File.separatorChar}data${File.separatorChar}InformativeMC${File.separatorChar}IMC-Core.data"

val tmpDirPath = "$cwd${File.separatorChar}tmp${File.separatorChar}InformativeMC"

@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
object ModEntryPoint : ModInitializer {
    private val LOGGER = LogManager.getLogger("IMC-Core")
    private const val MOD_ID = "informative_mc_api_core"
    private val modTimer = Timer("IMC Timer")
    lateinit var server: MinecraftServer
    private lateinit var config: ModConfig
    lateinit var data: ModData

    private fun createDirsIfNeeded() {
        arrayOf(modConfigDirPath, modDataDirPath).forEach {
            val file = File(it)
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    LOGGER.error("Failed when creating directory $it")
                }
            }
        }
    }

    private inline fun <reified T> safeLoadFile(
        path: String,
        default: T,
        createAndWriteIfAbsent: Boolean = true
    ): T {
        val file = getFile(path)
        val obj = if (file.exists()) {
            try {
                Json.decodeFromStream<T>(file.inputStream())
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
                LOGGER.info("Load file failed. Creating a default one...")
                if (file.createNewFile()) {
                    saveToFileLocked(default, file)
                } else {
                    LOGGER.error("Creating default file failed.")
                }
            }
            default
        }
        return obj
    }

    private fun loadConfig() {
        config = safeLoadFile(modConfigFilePath, ModConfig.DEFAULT)
        ModConfig.CURRENT = config
    }

    private fun loadData() {
        data = safeLoadFile(modDataFilePath, ModData.DEFAULT)
        ModData.CURRENT = data
    }

    private fun registerServerCallback() {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            if (server == null) {
                throw NullPointerException("Cannot access current server!")
            } else {
                this.server = server
            }
        }
    }

    override fun onInitialize() {
        createDirsIfNeeded()
        loadConfig()
        loadData()
        registerServerCallback()
        ApiServer.setup()
    }
}
