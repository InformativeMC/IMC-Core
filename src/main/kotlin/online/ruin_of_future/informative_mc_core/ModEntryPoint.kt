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
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


val modConfigDirPath = run {
    val cwd = System.getProperty("user.dir")
    if (cwd == null || cwd.isEmpty()) {
        throw IOException("Cannot access current working directory")
    } else {
        if (cwd.endsWith(File.separatorChar)) {
            cwd.trimEnd(File.separatorChar)
        }
    }
    "$cwd${File.separatorChar}config${File.separatorChar}InformativeMC"
}

val modConfigFilePath = run {

    val modConfigDirFile = File(modConfigDirPath)
    if (!modConfigDirFile.exists()) {
        if (!modConfigDirFile.mkdirs()) {
            throw IOException("Config directory does not exist and cannot be created.")
        }
    }

    "$modConfigDirPath${File.separatorChar}IMC-Core.json"
}

@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
object ModEntryPoint : ModInitializer {
    private val LOGGER = LogManager.getLogger("IMC-Core")
    private const val MOD_ID = "informative_mc_api_core"
    private val modTimer = Timer("IMC Timer")
    lateinit var server: MinecraftServer
    private var config: ModConfig

    private fun getOrCreateConfigFile(): File {
        val file = getFile(modConfigFilePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    init {
        // Check config
        val configFile = getFile(modConfigFilePath)
        config = if (configFile.exists()) {
            try {
                Json.decodeFromStream(configFile.inputStream())
            } catch (e: Exception) {
                val cpyFile = File("${configFile.absoluteFile.parent}${File.separatorChar}OLD-${configFile.name}")
                cpyFile.writeBytes(configFile.readBytes())
                LOGGER.warn("Error occurs when reading config file. Creating a default config instead.")
                LOGGER.warn("Old config file would be renamed:")
                LOGGER.warn("\t${configFile.absolutePath}")
                LOGGER.warn("\t\t||")
                LOGGER.warn("\t\t\\/")
                LOGGER.warn("\t${cpyFile.absolutePath}")
                ModConfig.DEFAULT
            }
        } else {
            ModConfig.DEFAULT
        }
        ModConfig.CURRENT = config
        // Start a coroutine to save config periodically
        modTimer.schedule(TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(5)) {
            saveToFileLocked(config, getOrCreateConfigFile())
        }
    }

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            if (server == null) {
                throw NullPointerException("Cannot access current server!")
            } else {
                this.server = server
            }
        }
        ApiServer.setup()
    }
}
