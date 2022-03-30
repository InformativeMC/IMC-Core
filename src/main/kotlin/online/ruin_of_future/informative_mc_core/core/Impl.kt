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
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import online.ruin_of_future.informative_mc_core.command.ImcCommand
import online.ruin_of_future.informative_mc_core.config.ModConfig
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.util.configDir
import online.ruin_of_future.informative_mc_core.util.gameDir
import online.ruin_of_future.informative_mc_core.web_api.ApiServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Path
import java.util.*

val cwd: Path = gameDir.toAbsolutePath()

val modConfigDirPath: Path = configDir.resolve("InformativeMC").toAbsolutePath()
val modConfigFilePath: Path = modConfigDirPath.resolve("IMC-Core.json").toAbsolutePath()

val modDataDirPath: Path = cwd.resolve("mods").resolve("InformativeMC").toAbsolutePath()
val modDataFilePath: Path = modDataDirPath.resolve("IMC-Core.data").toAbsolutePath()

val tmpDirPath: Path = cwd.resolve("tmp").resolve("InformativeMC").toAbsolutePath()

val dbFilePath: Path = modDataDirPath.resolve("IMC.db").toAbsolutePath()

@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
sealed class ImcCoreImpl : ModInitializer {

    companion object {
        @JvmStatic
        protected val LOGGER: Logger = LogManager.getLogger("IMC-Core")
        protected const val MOD_ID = "informative_mc_api_core"
    }

    protected val modTimer = Timer("IMC Timer")
    lateinit var server: MinecraftServer
    private lateinit var config: ModConfig
    lateinit var modData: ModData
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


    private fun loadConfig() {
        LOGGER.info("Loading IMC config...")
        config = ModConfig.load()
        // TODO: Write on demand
        LOGGER.info("IMC config loaded.")
    }

    private fun loadData() {
        LOGGER.info("Loading IMC data...")
        modData = ModData.load()
        LOGGER.info("IMC data loaded.")
    }

    private fun setupApiServer() {
        LOGGER.info("Starting IMC API server...")
        apiServer = ApiServer(config, modData)
        LOGGER.info("IMC API server started.")
    }

    private fun setupImcCommand() {
        LOGGER.info("Setting up IMC commands...")
        imcCommand = ImcCommand(modData, this.isTestImpl)
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