/*
 * Copyright (c) 2022 InformativeMC
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/lgpl-3.0.txt>.
 */
package online.ruin_of_future.informative_mc_core.core

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import online.ruin_of_future.informative_mc_core.command.ImcCommand
import online.ruin_of_future.informative_mc_core.config.ModConfig
import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.util.configDir
import online.ruin_of_future.informative_mc_core.util.gameDir
import online.ruin_of_future.informative_mc_core.util.getFile
import online.ruin_of_future.informative_mc_core.util.saveToFile
import online.ruin_of_future.informative_mc_core.web_api.ApiServer
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Path
import java.util.*

val cwd: Path = gameDir.toAbsolutePath()

val modConfigDirPath: Path = configDir.resolve("InformativeMC").toAbsolutePath()
val modConfigFilePath: Path = modConfigDirPath.resolve("IMC-Core.json").toAbsolutePath()

val modDataDirPath: Path = cwd.resolve("mods").resolve("InformativeMC").toAbsolutePath()
val modDataFilePath: Path = modDataDirPath.resolve("IMC-Core.data").toAbsolutePath()

val tmpDirPath: Path = cwd.resolve("tmp").resolve("InformativeMC").toAbsolutePath()

@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
sealed class ImcCoreImpl : ModInitializer {
    protected val LOGGER = LogManager.getLogger("IMC-Core")
    protected val MOD_ID = "informative_mc_api_core"
    protected val modTimer = Timer("IMC Timer")
    lateinit var server: MinecraftServer
    private lateinit var config: ModConfig
    lateinit var modDataManager: ModDataManager
    private lateinit var apiServer: ApiServer
    private lateinit var imcCommand: ImcCommand

    open val isTestImpl: Boolean = false

    private fun createDirsIfNeeded() {
        arrayOf(modConfigDirPath, modDataDirPath).forEach {
            val file = File(it.toString())
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

    private inline fun <reified T> safeLoadFile(
        path: Path,
        default: T,
        createAndWriteIfAbsent: Boolean = true
    ): T {
        return safeLoadFile(path.toString(), default, createAndWriteIfAbsent)
    }

    private fun loadConfig() {
        LOGGER.info("Loading IMC config...")
        config = safeLoadFile(modConfigFilePath, ModConfig.DEFAULT)
        // TODO: Write on demand
//        modTimer.schedule(
//            delay = TimeUnit.SECONDS.toMillis(1),
//            period = TimeUnit.MINUTES.toMillis(5),
//        ) {
//            saveToFileLocked(data, modConfigFilePath)
//        }
        LOGGER.info("IMC config loaded.")
    }

    private fun loadData() {
        LOGGER.info("Loading IMC data...")
        modDataManager = safeLoadFile(modDataFilePath, ModDataManager.DEFAULT)
        // TODO: Write on demand
        // TODO: Replace it with a Database
//        modTimer.schedule(
//            delay = TimeUnit.SECONDS.toMillis(1),
//            period = TimeUnit.MINUTES.toMillis(5),
//        ) {
//            saveToFileLocked(data, modDataFilePath)
//        }
        LOGGER.info("IMC data loaded.")
    }

    private fun setupApiServer() {
        LOGGER.info("Starting IMC API server...")
        apiServer = ApiServer(server, config.port, config, modDataManager)
        LOGGER.info("IMC API server started.")
    }

    private fun setupImcCommand() {
        LOGGER.info("Setting up IMC commands...")
        imcCommand = ImcCommand(modDataManager, this.isTestImpl)
        imcCommand.setup()
        LOGGER.info("IMC commands set up.")
    }

    private fun mcServerCallback(server: MinecraftServer?) {
        if (server == null) {
            throw NullPointerException("Cannot access current server!")
        } else {
            this.server = server
        }
        // Restful api server
        setupApiServer()
        LOGGER.info("Minecraft server started.")
    }

    private fun registerMcServerCallback() {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            mcServerCallback(server)
        }
    }

    override fun onInitialize() {
        // Config and data
        createDirsIfNeeded()
        loadConfig()
        loadData()
        // MC command
        setupImcCommand()
        // Miscellaneous
        registerMcServerCallback()
    }
}