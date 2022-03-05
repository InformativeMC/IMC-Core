package online.ruin_of_future.informative_mc_core

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import online.ruin_of_future.informative_mc_core.web_api.ApiServer
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
object ModEntryPoint : ModInitializer {
    private val LOGGER = LogManager.getLogger("IMC-Core")
    private const val MOD_ID = "informative_mc_api_core"
    private const val MOD_CONFIG_DIR = "InformativeMC"
    private const val CONFIG_NAME = "IMC-Core.json"
    private val modTimer = Timer("IMC Timer")

    lateinit var server: MinecraftServer

    private val CONFIG_ROOT = run {
        val path = "$FILE_ROOT${File.separatorChar}config${File.separatorChar}$MOD_CONFIG_DIR"
        val file = File(path)
        if (!file.exists()) {
            if (file.mkdirs()) {
                return@run path
            } else {
                throw IOException("Config directory does not exist and cannot be created.")
            }
        } else {
            return@run path
        }
    }

    private fun getOrCreateConfigFile(): File {
        val file = getFile(CONFIG_ROOT, CONFIG_NAME)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    val config: ModConfig = run {
        val configFile = getOrCreateConfigFile()
        try {
            return@run Json.decodeFromStream<ModConfig>(configFile.inputStream())
        } catch (ioE: IOException) {
            LOGGER.error("Cannot read config file")
            throw ioE
        } catch (serializationE: SerializationException) {
            LOGGER.warn("Decoding config file failed, using a default config")
            LOGGER.info("Copying old file")
            configFile.renameTo(getFile(CONFIG_ROOT, "OLD-$CONFIG_NAME"))
            return@run ModConfig.DEFAULT
        }
    }

    init {
        // Start a coroutine to save config periodically
        modTimer.schedule(TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(5)) {
//            saveToFile(Json.encodeToString(config), getOrCreateConfigFile())
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
